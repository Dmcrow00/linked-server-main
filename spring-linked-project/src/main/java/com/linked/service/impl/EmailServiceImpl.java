package com.linked.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Envío de emails transaccionales con plantillas Thymeleaf ({@code templates/email}).
 * <p>
 * Los envíos son asíncronos: si falla el servidor de correo se registra en el log, pero no
 * se interrumpe la operación que originó el email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@linked-app.com}")
    private String fromAddress;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    /** Envía el enlace de recuperación, que apunta a la pantalla {@code /reset-password} del cliente web. */
    @Async
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        try {
            Context ctx = new Context();
            ctx.setVariable("username", username);
            ctx.setVariable("resetUrl", frontendBaseUrl + "/reset-password?token=" + resetToken);
            // Debe coincidir con el vencimiento que fija UserServiceImpl#forgotPassword.
            ctx.setVariable("expirationHours", 1);

            String html = templateEngine.process("email/password-reset", ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Restablecer contraseña - LINKED");
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }
}
