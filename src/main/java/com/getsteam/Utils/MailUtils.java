package com.getsteam.Utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MailUtils {
    @Resource
    private JavaMailSenderImpl mailSender;
    @Value("${spring.mail.username}")
    private String mailfrom;
    // 发送简单邮件
    public void sendSimpleEmail(String mailto, String title, String content) {
        //  定制邮件发送内容
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailfrom);
        message.setTo(mailto);
        message.setSubject(title);
        message.setText(content);
        // 发送邮件
        mailSender.send(message);
    }
}
