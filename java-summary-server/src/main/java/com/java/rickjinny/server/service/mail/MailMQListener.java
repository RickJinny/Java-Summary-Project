package com.java.rickjinny.server.service.mail;

import com.java.rickjinny.server.bean.MailRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 邮件发送的监听器 - 消费者
 */
public class MailMQListener {

    private static final Logger logger = LoggerFactory.getLogger(MailMQListener.class);

    @Autowired
    private MailService mailService;

    /**
     * 监听消费邮件发送的消息
     */
    @RabbitListener(queues = {"${mq.email.queue}"}, containerFactory = "multiListenerContainer")
    public void consumeMessage(MailRequest request) {
        try {
            logger.info("监听消费邮件发送的消息 - 监听到消息：{}", request);
            if (request != null && StringUtils.isNotBlank(request.getUserMails())) {
                mailService.sendSimpleEmail(request.getSubject(), request.getContent(),
                        StringUtils.split(request.getUserMails(), ","));
            }
        } catch (Exception e) {
            logger.error("监听消费邮件发送的消息 - 发生异常: {}", request, e);
        }
    }
}
