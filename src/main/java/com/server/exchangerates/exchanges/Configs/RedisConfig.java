package com.server.exchangerates.exchanges.Configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.server.exchangerates.exchanges.Response.ExchangeResponse;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;

@Configuration
public class RedisConfig {

    //connecting the redis server explicitly.
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Value("${spring.data.redis.username:default}")
    private String redisUsername;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean sslEnabled;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(){
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost , redisPort);
        config.setUsername(redisUsername);
        config.setPassword(redisPassword);
        
        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfig = LettuceClientConfiguration.builder();
        
        if (sslEnabled) {
            // Configure SSL for Redis Cloud
            SslOptions sslOptions = SslOptions.builder()
                .build();
            ClientOptions clientOptions = ClientOptions.builder()
                .sslOptions(sslOptions)
                .build();
            clientConfig.clientOptions(clientOptions);
            clientConfig.useSsl();
        }
        
        return new LettuceConnectionFactory(config, clientConfig.build());
    }

    @Bean
    public RedisTemplate<String , ExchangeResponse> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String , ExchangeResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer<ExchangeResponse> serializer = new Jackson2JsonRedisSerializer<>(ExchangeResponse.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        return template;
    }
}
