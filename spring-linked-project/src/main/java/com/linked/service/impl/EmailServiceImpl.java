package com.linked.service.impl;

import com.dmc00.core.swe.dto.SweUsrDto;
import com.linked.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;

/** Implementación de {@link EmailService} con plantillas Thymeleaf ({@code templates/email}). */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String remitente;
    private final String urlDelCliente;
    private final Duration vigenciaRecuperacion;

    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine,
                            @Value("${app.mail.from:noreply@linked-app.com}") String remitente,
                            @Value("${app.frontend.base-url:http://localhost:4200}") String urlDelCliente,
                            @Value("${app.swe.vigencia-recuperacion:1h}") Duration vigenciaRecuperacion) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.remitente = remitente;
        this.urlDelCliente = urlDelCliente;
        this.vigenciaRecuperacion = vigenciaRecuperacion;
    }

    @Override
    @Async
    public void enviarRecuperacionDeContrasena(SweUsrDto.PasswordReset recuperacion) {
        try {
            Context ctx = new Context();
            ctx.setVariable("username", recuperacion.username());
            ctx.setVariable("resetUrl", urlDelCliente + "/reset-password?token=" + recuperacion.token());
            ctx.setVariable("expirationHours", Math.max(1, vigenciaRecuperacion.toHours()));
            String html = templateEngine.process("email/password-reset", ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(recuperacion.email());
            helper.setSubject("Restablecer contraseña - LINKED");
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Mail de recuperación de contraseña enviado a {}", recuperacion.username());
        } catch (MessagingException | MailException ex) {
            log.error("No se pudo mandar el mail de recuperación a {}: {}", recuperacion.username(), ex.getMessage());
        }
    }
}
