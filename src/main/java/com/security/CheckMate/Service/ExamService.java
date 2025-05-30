package com.security.CheckMate.Service;

import com.security.CheckMate.DTO.ExamCreateDto;
import com.security.CheckMate.Domain.User;
import com.security.CheckMate.Security.Cryptogram;
import com.security.CheckMate.Security.Envelope;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

@Service
public class ExamService {

    public void makeExamAnswer(ExamCreateDto examCreateDto, HttpSession session) {
        session.setAttribute("publicKeyBytes", examCreateDto.getPublicKeyBytes()); // 예시
    }

    public void encryptAnswer(User user) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Envelope envelope = new Envelope();

        String publicKeyFname = "public" + user.getUserName() + ".txt";
        FileInputStream fis = new FileInputStream(publicKeyFname);
        ObjectInputStream os = new ObjectInputStream(fis);
        PublicKey publicKey = (PublicKey) os.readObject();

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();

        envelope.encrypt(publicKey, secretKey, user);
        Cryptogram cryptogram = new Cryptogram();
    }

    public void verifyExamAnswer(HttpSession session) {
        byte[] publicKeyBytes = (byte[]) session.getAttribute("publicKeyBytes");
    }
}
