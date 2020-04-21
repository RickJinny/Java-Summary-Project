package com.java.rickjinny.server.service.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;

public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private Environment environment;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * 发送简单的邮件信息
     */
    @Async("threadPoolTaskExecutor")
    public void sendSimpleEmail(String subject, String content, String... tos) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject(subject);
            message.setText(content);
            message.setTo(tos);
            message.setFrom(environment.getProperty("mail.send.from"));
            mailSender.send(message);
            logger.info("- 发送简单的邮件消息完毕 -");
        } catch (Exception e) {
            logger.error("- 发送简单的邮件消息, 发生异常: ", e.fillInStackTrace());
        }
    }
}
