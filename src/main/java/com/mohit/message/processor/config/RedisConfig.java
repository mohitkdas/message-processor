package com.mohit.message.processor.config;

import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Collections;

@Configuration
public class RedisConfig {

    private static final String REDIS_CLUSTER_ENDPOINT = "clustercfg.redis-cache.yq50wk.use1.cache.amazonaws.com";
    private static final int REDIS_PORT = 6379;

    @Bean(destroyMethod = "shutdown")
    public RedisClusterClient redisClusterClient() {
        RedisURI redisURI = RedisURI.builder()
                .withHost(REDIS_CLUSTER_ENDPOINT)
                .withPort(REDIS_PORT)
                .withSsl(true)
                .withTimeout(Duration.ofSeconds(5))
                .build();

        return RedisClusterClient.create(Collections.singletonList(redisURI));
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisClusterConnection<String, String> redisClusterConnection(RedisClusterClient client) {
        return client.connect();
    }

    @Bean
    public RedisAdvancedClusterCommands<String, String> redisCommands(StatefulRedisClusterConnection<String, String> connection) {
        return connection.sync();
    }
}
