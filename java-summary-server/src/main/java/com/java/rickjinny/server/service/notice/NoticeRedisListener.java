package com.java.rickjinny.server.service.notice;

import com.google.gson.Gson;
import com.java.rickjinny.model.entity.Notice;
import com.java.rickjinny.server.service.mail.MailService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * redis的订阅发布机制 - 监听通告公告消息 的消费者listener
 */
@Service("noticeRedisListener")
public class NoticeRedisListener {

    private static final Logger log = LoggerFactory.getLogger(NoticeRedisListener.class);

    @Autowired
    private MailService mailService;

    // 订阅 监听 并 处理 channel中的消息 - sub
    public void listenMsg(String message) {
        try {
            log.info("--订阅 监听 并 处理redis channel中的消息: {}", message);
            if (StringUtils.isNotBlank(message) && message.contains("{")) {
                Notice notice = new Gson().fromJson(message, Notice.class);
                mailService.sendSimpleEmail(notice.getTitle(), notice.getContent(), "111@126.com");
            }
        } catch (Exception e) {
            log.error("订阅 监听 并 处理redis channel中的消息-发生异常：{}", message, e);
        }
    }
}






















