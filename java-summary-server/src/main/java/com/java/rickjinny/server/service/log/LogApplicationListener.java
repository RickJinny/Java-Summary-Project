package com.java.rickjinny.server.service.log;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 监听的消息事件 LogApplicationEvent
 */
@Component
public class LogApplicationListener implements ApplicationListener<LogApplicationEvent> {

    @Override
    public void onApplicationEvent(LogApplicationEvent logApplicationEvent) {

    }
}
