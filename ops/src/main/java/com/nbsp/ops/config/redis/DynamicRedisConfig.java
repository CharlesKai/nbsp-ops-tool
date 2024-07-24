package com.nbsp.ops.config.redis;

import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

/**
 * @author charlesYan
 * @description: redis动态配置
 * @date 2024年07月22日
 */
@Component
public class DynamicRedisConfig {

  public RedisTemplate<String, String> createRedisTemplate(String host, int port, String password) {
    return createRedisTemplate(host, port, password, -1);
  }

  public RedisTemplate<String, String> createRedisTemplate(
      String host, int port, String password, int database) {
    LettuceConnectionFactory connectionFactory =
        createLettuceConnectionFactory(host, port, password, database);
    // 关闭共享链接
    connectionFactory.setShareNativeConnection(false);
    connectionFactory.afterPropertiesSet(); // 必须初始化实例

    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new StringRedisSerializer());
    template.afterPropertiesSet(); // 必须初始化模板
    return template;
  }

  public LettuceConnectionFactory createLettuceConnectionFactory(
      String host, int port, String password, int database) {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(host);
    redisStandaloneConfiguration.setPort(port);
    redisStandaloneConfiguration.setPassword(password);
    if (database >= 0) {
      redisStandaloneConfiguration.setDatabase(database);
    }
    LettuceClientConfiguration clientConfiguration =
        LettucePoolingClientConfiguration.builder()
            .clientResources(DefaultClientResources.create())
            .build();
    return new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfiguration);
  }

  private GenericObjectPoolConfig<?> createPoolConfig(
      int minIdle, int maxIdle, int maxTotal, long maxWaitMillis) {
    GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
    // 最大的空闲连接数
    poolConfig.setMaxIdle(10);
    // 最小的空闲连接数
    poolConfig.setMinIdle(1);
    // 最大的总连接数
    poolConfig.setMaxTotal(20);
    // 获取连接的最大等待时间
    poolConfig.setMaxWaitMillis(10000);
    return poolConfig;
  }
}
