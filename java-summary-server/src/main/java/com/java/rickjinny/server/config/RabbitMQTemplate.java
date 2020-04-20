package com.java.rickjinny.server.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * RabbitMQ 模板操作 bean 组件实例的自定义注解
 */
@Configuration
public class RabbitMQTemplate {

    @Autowired
    private Environment env;

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
