package com.vansh.manger.Manger.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.message.SimpleMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendNewUserWelcomeEmail(String toEmail, String fullName, String rawPassword) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Welcome to Manger - Your School Portal Account");

        message.setText(
                "Hello " + fullName + ",\n\n" +
                        "An account has been created for you on the Manger school portal.\n\n" +
                        "Your login details are:\n" +
                        "Email: " + toEmail + "\n" +
                        "Temporary Password: " + rawPassword + "\n\n" +
                        "Please log in and change your password at your earliest convenience.\n\n" +
                        "Regards,\n" +
                        "Your School Administration"
        );

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String fullName, String newRawPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Manger - Your Password Has Been Reset");
        message.setText(
                "Hello " + fullName + ",\n\n" +
                        "An administrator has reset the password for your account.\n\n" +
                        "Your new temporary password is: " + newRawPassword + "\n\n" +
                        "Please log in and change this password immediately.\n\n" +
                        "Regards,\n" +
                        "Your School Administration"
        );
        mailSender.send(message);
    }

    public void sendMarksheet(String to, byte[] pdfBytes, String studentName, String examName,  String rollNo,  String subjectName) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(examName + "Your Marksheet for " + subjectName);

            helper.setText(
                    "Dear " + studentName + "{" + rollNo + "}" + "," + "\n\n"
                    +  "Your marks for " + subjectName + " have been recorded.\n" + "Please find your marksheet attached below. \n\n"
                    + "Best regards, \nManger Team"
            );

            helper.addAttachment("Marksheet.pdf", new ByteArrayResource(pdfBytes));

            mailSender.send(message);




        }catch (Exception e) {
            throw new RuntimeException("Failed to send email: "+ e.getMessage());
        }
    }
}
