package com.xin.xinoj.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
//生成get，set方法
@Data
public class redissonConfig {
    private String host;
    private String port;

    @Bean
    public RedissonClient redisClient(){
        Config redissonConfig = new Config();
        String address = String.format("redis://%s:%s",host,port);
        redissonConfig.useSingleServer().setAddress(address).setPassword("lijun456789").setDatabase(0);
        return Redisson.create(redissonConfig);
    }
}
