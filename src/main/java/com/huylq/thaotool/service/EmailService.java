package com.huylq.thaotool.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @Value("${spring.mail.username}")
  private String FROM_ADDRESS;

  private static final String SENDER_NAME = "THAODP-TOOL";

  @Synchronized
  protected void sendEmail(String toAddress, String subject, String templateName, String filePath, Map<String, Object> variables) throws MessagingException, UnsupportedEncodingException {
    log.info("started to send email to {}",  toAddress);
    Context context = new Context();
    context.setVariables(variables);

    String content = templateEngine.process(templateName, context);

    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);

    helper.setFrom(FROM_ADDRESS, SENDER_NAME);
    helper.setTo(toAddress);
    helper.setSubject(subject);
    helper.setText(content, true);

    File attachment = new File(filePath);
    if (attachment.exists()) {
      helper.addAttachment(attachment.getName(), attachment);
    } else {
      throw new MessagingException("Attachment file does not exist: " + filePath);
    }

    mailSender.send(message);
  }

}
