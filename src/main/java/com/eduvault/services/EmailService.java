package com.eduvault.services;

import com.eduvault.user.enums.AccountStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Year;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;


    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            System.out.println("Plain text email sent successfully to " + to);
        } catch (Exception e) {
            System.err.println("Error sending plain text email: " + e.getMessage());
        }
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            System.out.println("HTML email sent successfully to " + to);
        } catch (Exception e) {
            System.err.println("Error sending HTML email: " + e.getMessage());
        }
    }

    @Async
    public void sendInvitationEmail(String to, String subject, String invitationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            Context context = new Context();
            context.setVariable("invitationLink", invitationLink);
            context.setVariable("year", Year.now().getValue());

            String htmlContent = templateEngine.process("invitation-email.html", context);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);
            System.out.println("Invitation email sent successfully to " + to);
        } catch (Exception e) {
            System.err.println("Error sending invitation email: " + e.getMessage());
        }
    }

    @Async
    public void sendReceiptRejectionEmail(String to, String reason, String reuploadLink, String matricNumber, String document) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            Context context = new Context();
            context.setVariable("matricNumber", matricNumber);
            context.setVariable("reason", reason);
            context.setVariable("document", document);
            context.setVariable("reuploadLink", reuploadLink);
            context.setVariable("year", Year.now().getValue());

            String htmlContent = templateEngine.process("receipt-rejection-email", context);

            helper.setTo(to);
            helper.setSubject("Receipt Rejected ❌");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

            System.out.println("Invitation email sent successfully to " + to);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send rejection email", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("resetLink", resetLink);
        String htmlContent = templateEngine.process("forgot-password", context);

        sendHtmlEmail(to, "Password Reset Request", htmlContent);
    }

    @Async
    public void sendPasswordResetSuccessEmail(String to, String name) {
        Context context = new Context();
        context.setVariable("name", name);
        String htmlContent = templateEngine.process("password-reset-success", context);

        sendHtmlEmail(to, "Password Reset Successful", htmlContent);
    }


    @Async
    public void sendAccountStatusEmail(String recipientEmail, String fullName, AccountStatus status) {
        String templateName = switch (status) {
            case SUSPENDED -> "account-suspended";
            case DEACTIVATED -> "account-deactivated";
            case ACTIVE -> "account-reactivated";
        };

        String subject = switch (status) {
            case SUSPENDED -> "Account Suspended - EduVault";
            case DEACTIVATED -> "Account Deactivated - EduVault";
            case ACTIVE -> "Account Reactivated - EduVault";
        };

        Context context = new Context();
        context.setVariable("fullName", fullName);

        String htmlContent = templateEngine.process(templateName, context);

        sendHtmlEmail(recipientEmail, "Account Status Update", htmlContent);

    }
}
