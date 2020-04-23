package com.java.rickjinny.server.service.notice;

import com.java.rickjinny.server.enums.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * 用户消息-订阅发布
 */
@Configuration
public class NoticeRedisConfig {

    @Autowired
    private NoticeRedisListener noticeRedisListener;

    /**
     * redis消息监听器容器-发布订阅时候都是通过此容器完成
     *
     * @param factory
     * @param listenerAdapter
     * @return
     */
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory factory, MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        // 添加一个到多个 topic (频道)
        container.addMessageListener(listenerAdapter, new PatternTopic(Constant.RedisTopicNameEmail));
        return container;
    }

    @Bean
    public RedisConnectionFactory factory(){
        return new JedisConnectionFactory();
    }

    /**
     * 绑定消息 - 消息监听器 - 监听接收消息的方法
     * @return
     */
    @Bean
    public MessageListenerAdapter listenerAdapter(){
        return new MessageListenerAdapter(noticeRedisListener, "listenMsg");
    }
}
