package com.victorqueiroga.serverwatch.mail;

import java.io.File;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailServiceImpl {
    
    private final JavaMailSender mailSender;

    public void sendSimpleEmail(String to, String subject, String body) {
        // Implementação do envio de email usando mailSender
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendMessageWithAttachment(String to, String subject, String body, String attachmentPath) throws MessagingException {
        // Implementação do envio de email com anexo
        // Este método pode ser implementado conforme necessário
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);
        helper.addAttachment("attachment", new File(attachmentPath));
        mailSender.send(mimeMessage);
    }
}
