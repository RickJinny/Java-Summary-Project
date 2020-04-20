package com.java.rickjinny.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * RabbitMQ 模板操作 bean 组件实例的自定义注解
 */
@Configuration
public class RabbitMQTemplate {

    public static final Logger logger = LoggerFactory.getLogger(RabbitTemplate.class);

    @Autowired
    private Environment env;

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    private SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer;

    /**
     * rabbitmq 自定义注入模板操作组件
     */
    @Bean
    public RabbitTemplate rabbitTemplate() {
        connectionFactory.setPublisherConfirms(true);
        connectionFactory.setPublisherReturns(true);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String s) {
                logger.info("消息发送成功: correlationData={}, ack={}, s={}", correlationData, ack, s);
            }
        });

        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {

            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String rountingKey) {
                logger.info("消息丢失: message={}, replyCode={}, replyText={}, exchange={}, rountingKey={}",
                        message, replyCode, replyText, exchange, rountingKey);
            }

        });
        return rabbitTemplate;
    }

    /**
     * 预先创建交换机、路由及其绑定
     */
    @Bean
    public TopicExchange logExchange() {
        return new TopicExchange(env.getProperty("mq.log.exchange"), true, false);
    }

    @Bean
    public Queue logQueue() {
        return new Queue(env.getProperty("mq.log.queue"), true);
    }

    @Bean
    public Binding logBinding() {
        return BindingBuilder.bind(logQueue())
                .to(logExchange())
                .with(env.getProperty("mq.log.routing.key"));
    }
}
