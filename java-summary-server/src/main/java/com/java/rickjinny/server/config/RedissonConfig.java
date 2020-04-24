package com.java.rickjinny.server.config;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 自定义注入 Redission 的操作Bean
 */
@Configuration
public class RedissonConfig {

    private Environment environment;

    /**
     * 单节点
     * @return
     */
//    @Bean
//    public RedissonClient client() {
//        Config config = new Config();
//        config.useSingleServer().setAddress(environment.getProperty("redisson.url.single"));
//        RedissonClient client = Redisson.create(config);
//        return client;
//    }

    /**
     * 集群
     */
    @Bean
    public RedissonClient client() {
        Config config = new Config();
        config.useClusterServers().setScanInterval(2000)
                .addNodeAddress(StringUtils.split(environment.getProperty("redission.url.cluster"), ","))
                .setMasterConnectionMinimumIdleSize(10)
                .setMasterConnectionPoolSize(64)
                .setSlaveConnectionMinimumIdleSize(10)
                .setSlaveConnectionPoolSize(64)
                .setTimeout(15000);
        RedissonClient client = Redisson.create(config);
        return client;
    }
}
