package com.java.rickjinny.server.service.log;

import com.java.rickjinny.model.entity.SysLog;
import com.java.rickjinny.server.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 监听的消息事件 LogApplicationEvent  (消息的监听器)
 */
@Component
public class LogApplicationListener implements ApplicationListener<LogApplicationEvent> {

    public static final Logger logger = LoggerFactory.getLogger(LoggerFactory.class);

    @Autowired
    private LogService logService;

    /**
     * 监听并且处理消息
     */
    @Override
    @Async("threadPoolTaskExecutor")
    public void onApplicationEvent(LogApplicationEvent event) {
        logger.info("Spring的消息驱动模型-监听并处理消息: {}", event);
        try {
            SysLog sysLog = new SysLog(event.getUserName(), event.getOperation(), event.getMethod());
            logService.recordLog(sysLog);
        } catch (Exception e) {
            logger.error("Spring的消息驱动模型-监听并处理消息, 发生异常", e);
        }
    }
}
