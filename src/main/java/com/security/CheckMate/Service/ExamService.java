package com.security.CheckMate.Service;

import com.google.gson.Gson;
import com.security.CheckMate.DTO.ExamCommand;
import com.security.CheckMate.Domain.ExamAnswer;
import com.security.CheckMate.Domain.User;
import com.security.CheckMate.Security.AsymmetricKeyManager;
import com.security.CheckMate.Security.Cryptogram;
import com.security.CheckMate.Security.Envelope;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.*;
import java.util.Arrays;

@Service
public class ExamService {

    public void makeExamAnswer(ExamCommand examCommand, String json, HttpSession session) {
        User user = new User(examCommand.getStudentId(), "student", null);
        ExamAnswer Q1answer = new ExamAnswer(user.getUserName(), examCommand.getAnswer1());
        ExamAnswer Q2answer = new ExamAnswer(user.getUserName(), examCommand.getAnswer2());

        // 나중에 DB 저장 또는 파일 저장 로직
        // 지금은 암호화로만 넘겨도 OK
        try {
            //encryptAnswer(session, json, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void encryptAnswer(HttpSession session, String json, User user, User professor) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, SignatureException, IllegalBlockSizeException, BadPaddingException {
        Envelope envelope = new Envelope();

        //교수님 공개키로 암호화해야
        String publicKeyFname = "public" + professor.getUserName() + ".txt";
        FileInputStream fis = new FileInputStream(publicKeyFname);
        ObjectInputStream os = new ObjectInputStream(fis);
        PublicKey publicKey = (PublicKey) os.readObject();

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);

        byte[] keyBytes = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(keyBytes);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        System.out.println(Arrays.toString(secretKey.getEncoded()));

        System.out.println("Session ID in submitAnswer: " + session.getId());

        //session.setAttribute("secretKeyBytes", keyBytes);
        session.setAttribute("secretKeyBytes", keyBytes.clone());
        envelope.encrypt(publicKey, keyBytes, user);

        Cryptogram cryptogram = new Cryptogram();
        cryptogram.encrypt(/*"plain"  + user.getUserName() + ".txt", "hash" + user.getUserName() + ".txt", */user, secretKey, json);

        Arrays.fill(keyBytes, (byte) 0);
        secretKey = null;
    }

    public boolean verifyExamAnswer(HttpSession session, User student, User professor) {
        try {
            byte[] publicKeyBytes = (byte[]) session.getAttribute("publicKeyBytes");
            // 검증 로직 ...
            //byte[] keyBytes = (byte[])session.getAttribute("secretKeyBytes");

            //byte[] keyBytes = new byte[32];
            //SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

            AsymmetricKeyManager keyMan = new AsymmetricKeyManager();
            PrivateKey privateKey = keyMan.loadPrivateKey("private" + professor.getUserName() + ".txt");

            Envelope envelope = new Envelope();
            SecretKey secretKey = envelope.decrypt(privateKey, student);
            System.out.println(Arrays.toString(secretKey.getEncoded()));

            System.out.println("Session ID in verifyExamAnswer: " + session.getId());

            Cryptogram cryptogram = new Cryptogram();
            cryptogram.decrypt(student, secretKey);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public int getScoreForUser(User user) {
        return 100;
    }

    public String toJson(ExamCommand examCommand) {
        Gson gson = new Gson();
        String json = gson.toJson(examCommand);

        System.out.println("저장할 JSON 내용:");
        System.out.println(json);

        try (FileWriter writer = new FileWriter("exam_submission.json")) {
            writer.write(json);
            System.out.println("JSON 파일 저장 완료:");
        } catch (IOException e) {
            System.err.println("JSON 저장 실패:");
            e.printStackTrace();
        }
        return json;
    }
}
