package com.java.rickjinny.server.service.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.rickjinny.model.entity.SysLog;
import com.java.rickjinny.server.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 队列的监听消费者
 */
@Component
public class LogMQListener {

    private static final Logger logger = LoggerFactory.getLogger(LogMQListener.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogService logService;

    // 指定要监听的队列 (可以是多个) 以及监听消费处理消息的模式（单一消费实例 - 单一线程）
    @RabbitListener(queues = {"${mq.log.queue}"}, containerFactory = "singleListenerContainer")
    public void consumeLogMessage(@Payload byte[] msg) {
        try {
            logger.info("--日志记录的监听-消费者-监听到消息--");
            SysLog entity = objectMapper.readValue(msg, SysLog.class);
            logService.recordLog(entity);
        } catch (Exception e) {
            logger.error("日志记录的监听-消费者-发生异常: ", e);
        }
    }
}
