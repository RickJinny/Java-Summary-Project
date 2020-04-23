package com.java.rickjinny.server.common;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.rickjinny.model.entity.SysLog;
import com.java.rickjinny.server.enums.Constant;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class CommonService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 统一用户记录日志
     * @param obj
     * @param operation
     * @param method
     * @throws Exception
     */
    public void recordLog(final Object obj, final String operation, final String method) throws Exception {
        SysLog log = new SysLog(Constant.logOperateUser, operation, method, objectMapper.writeValueAsString(obj));
        rabbitTemplate.setExchange(env.getProperty("mq.log.exchange"));
        rabbitTemplate.setRoutingKey(env.getProperty("mq.log.routing.key"));
        Message msg = MessageBuilder.withBody(objectMapper.writeValueAsBytes(log))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        rabbitTemplate.send(msg);
    }
}