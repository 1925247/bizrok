package com.bizrok.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RateLimitingConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    // Rate limit configurations
    public static class RateLimitConfig {
        public static final Duration WINDOW_SIZE = Duration.ofMinutes(1);
        public static final int REQUESTS_PER_WINDOW = 60;
        public static final int LOGIN_ATTEMPTS_PER_WINDOW = 5;
        public static final Duration LOGIN_WINDOW_SIZE = Duration.ofMinutes(15);
        public static final Duration BLOCK_DURATION = Duration.ofHours(1);
    }
}