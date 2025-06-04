package com.security.CheckMate.Controller;

import com.security.CheckMate.DTO.ExamCommand;
import com.security.CheckMate.Domain.User;
import com.security.CheckMate.Security.AsymmetricKeyManager;
import com.security.CheckMate.Service.ExamService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

@Controller
public class ExamController {

    @Autowired
    ExamService examSvc;

    @PostMapping("/exam/submit")
    public String submitAnswer(@ModelAttribute ExamCommand examCommand, HttpSession session) throws NoSuchPaddingException, IOException, NoSuchAlgorithmException, InvalidKeyException, ClassNotFoundException, SignatureException, IllegalBlockSizeException, BadPaddingException {
        String json = examSvc.toJson(examCommand);
        session.setAttribute("examCommand", examCommand);
        //examSvc.makeExamAnswer(examCommand, json, session);
        System.out.println("submit Controller test");
        User user = (User) session.getAttribute("user");

        User professor = new User("20000000", "professor", new String[]{"웹코드보안"});

        examSvc.encryptAnswer(session, json, user, professor);
        return "submit";
    }

    @GetMapping("/exam/submit")
    public String showSubmitPage(@ModelAttribute ExamCommand examCommand, HttpSession session) throws NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, SignatureException, BadPaddingException, InvalidKeyException, ClassNotFoundException {
        String json = examSvc.toJson(examCommand);
        //examSvc.makeExamAnswer(examCommand, json, session);
        User user = (User) session.getAttribute("user");

        User professor = new User("20000000", "professor", new String[]{"웹코드보안"});

        examSvc.encryptAnswer(session, json, user, professor);
        return "submit";
    }

    @RequestMapping("/exam/verify")
    public String verifyExamAnswer(HttpSession session, Model model) {
        //boolean valid = examSvc.verifyExamAnswer(session);
        //model.addAttribute("verification", valid ? "검증 성공" : "검증 실패");
        return "verification_result";
    }

    @GetMapping("/exam")
    public String showExam(HttpSession session, Model model) throws NoSuchAlgorithmException {
        User user = new User("20221234", "student", new String[]{"웹코드보안", "소프트웨어시스템개발"});

        AsymmetricKeyManager keyMan = new AsymmetricKeyManager();
        keyMan.generate();
        keyMan.savePrivateKey("private" + user.getUserName() + ".txt");
        keyMan.savePublicKey("public" + user.getUserName() + ".txt");
        User professor = new User("20000000", "professor", new String[]{"웹코드보안"});
        keyMan.generate();
        keyMan.savePrivateKey("private" + professor.getUserName() + ".txt");
        keyMan.savePublicKey("public" + professor.getUserName() + ".txt");

        session.setAttribute("user", user);
        model.addAttribute("user", user);

        return "exam";
    }

    @RequestMapping("/score")
    public String showScore(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        int score = examSvc.getScoreForUser(user);
        model.addAttribute("user", user);
        model.addAttribute("score", score);

        return "score";
    }

    @GetMapping("/professor_exam")
    public String professorExamPage() {
        return "professor_exam";
    }

    @PostMapping("/professor_exam")
    public String processStudentId(@RequestParam("studentId") String studentId, HttpSession session, Model model) {
        if ("20221234".equals(studentId)) {
            User student = new User("20221234", "student", new String[]{"웹코드보안"});

            // 세션에서 기존 답안 꺼내기 (예시)
            ExamCommand examCommand = (ExamCommand) session.getAttribute("examCommand");

            if (examCommand == null) {
                examCommand = new ExamCommand();
                examCommand.setAnswer1("저장된 답안이 없습니다.");
                examCommand.setAnswer2("0");
            }

            model.addAttribute("message", "유효한 학번입니다.");
            model.addAttribute("user", student);
            model.addAttribute("examCommand", examCommand);

            User professor = new User("20000000", "professor", new String[]{"웹코드보안"});

            boolean valid = examSvc.verifyExamAnswer(session, student, professor);
            model.addAttribute("verification", valid ? "검증 성공" : "검증 실패");
        } else {
            model.addAttribute("message", "잘못된 학번입니다.");
        }
        return "professor_exam";
    }

}
