package com.officedubac.project.services;

import com.officedubac.project.dto.UserMailDTO;
import com.officedubac.project.models.BusinessResourceException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Boolean> sendAccountCreatedEmails(List<UserMailDTO> users, String subject) {
        try {
            for (UserMailDTO u : users) {
                try {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("title", "Bienvenue sur PortailBAC 🎉 !");
                    variables.put("message", "Merci de vous être inscrit, le compte est activé avec succès.");
                    variables.put("login", u.getLogin());
                    variables.put("password", u.getPasswordPlain());
                    variables.put("email", u.getEmail());
                    sendEmailAccountCreated(u.getEmail(), subject, variables);
                } catch (Exception ex) {
                    System.err.println("Erreur d'envoi pour : " + u.getEmail());
                    ex.printStackTrace();
                }
            }
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    @Async("threadPoolTaskExecutor")
    public void sendEmailAccountCreated(String to, String subject, Map<String, Object> variables) throws MessagingException {
        // Contexte Thymeleaf
        Context context = new Context();
        context.setVariables(variables);

        // Génération du HTML à partir du template
        String htmlContent = templateEngine.process("account-created", context);

        // Création du message
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        try{
            mailSender.send(mimeMessage);
            log.info("Message d'activation de compte envoye avec succes." + to);
        } catch(MailException ex){
            log.error("Sending message: message non envoyé pour une erreur." + ex);
            throw new BusinessResourceException("SendMessError", "Erreur d'envoi du message.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(Exception ex){
            log.error("Sending message: Une erreur inatteandue est rencontrée.");
            throw new BusinessResourceException("SendMessError", "Erreur d'envoi du message.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Async("threadPoolTaskExecutor")
    public void sendEmailPasswordReinitialised(String to, String subject, Map<String, Object> variables) throws MessagingException {
        // Contexte Thymeleaf
        Context context = new Context();
        context.setVariables(variables);
        // Génération du HTML à partir du template
        String htmlContent = templateEngine.process("account-reinitialised", context);

        // Création du message
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }
}