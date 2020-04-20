package com.java.rickjinny.server.service.log;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.io.Serializable;

/**
 * Spring的消息驱动模型: applicationEvent applicationListener
 */
@Data
public class LogApplicationEvent extends ApplicationEvent implements Serializable {

    private String userName;

    private String operation;

    private String method;

    public LogApplicationEvent(Object source, String userName, String operation, String method) {
        super(source);
        this.userName = userName;
        this.operation = operation;
        this.method = method;
    }

    public LogApplicationEvent(Object source) {
        super(source);
    }
}
