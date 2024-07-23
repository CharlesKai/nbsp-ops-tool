package com.nbsp.ops.util.redis;

import io.lettuce.core.resource.DefaultClientResources;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Cluster;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.StringUtils;

/** 调用update方法能够自动更新connection需要结合动态配置回调来完成 */
public class UpdatebleRedisConnectionFactory implements RedisConnectionFactory {

  private volatile RedisConnectionFactory redisConnectionFactory;

  @Autowired private volatile RedisProperties properties;

  public UpdatebleRedisConnectionFactory(RedisProperties redisProperties) {
    this.properties = redisProperties;
    init();
  }

  // @PostConstruct
  public void init() {
    LettuceConnectionFactory lettuceConnectionFactory =
        this.createLettuceConnectionFactory(properties);
    redisConnectionFactory = applyProperties(properties, lettuceConnectionFactory);
    lettuceConnectionFactory.afterPropertiesSet();
  }

  public void update() {
    LettuceConnectionFactory jedisConnectionFactory =
        this.createLettuceConnectionFactory(properties);
    redisConnectionFactory = applyProperties(properties, jedisConnectionFactory);
    jedisConnectionFactory.afterPropertiesSet();
  }

  @Override
  public RedisConnection getConnection() {
    return redisConnectionFactory.getConnection();
  }

  @Override
  public RedisClusterConnection getClusterConnection() {
    return redisConnectionFactory.getClusterConnection();
  }

  @Override
  public boolean getConvertPipelineAndTxResults() {
    return redisConnectionFactory.getConvertPipelineAndTxResults();
  }

  @Override
  public RedisSentinelConnection getSentinelConnection() {
    return redisConnectionFactory.getSentinelConnection();
  }

  @Override
  public DataAccessException translateExceptionIfPossible(RuntimeException e) {
    return redisConnectionFactory.translateExceptionIfPossible(e);
  }

  protected final LettuceConnectionFactory applyProperties(
      RedisProperties properties, LettuceConnectionFactory factory) {
    this.configureConnection(properties, factory);
    if (properties.isSsl()) {
      factory.setUseSsl(true);
    }
    factory.setDatabase(properties.getDatabase());
    if (properties.getTimeout().toMillis() > 0) {
      factory.setTimeout(properties.getTimeout().toMillis());
    }
    return factory;
  }

  private void configureConnection(RedisProperties properties, LettuceConnectionFactory factory) {
    if (StringUtils.hasText(properties.getUrl())) {
      this.configureConnectionFromUrl(properties, factory);
    } else {
      factory.setHostName(properties.getHost());
      factory.setPort(properties.getPort());
      if (properties.getPassword() != null) {
        factory.setPassword(properties.getPassword().trim());
      }
    }
  }

  private void configureConnectionFromUrl(
      RedisProperties properties, LettuceConnectionFactory factory) {
    String url = properties.getUrl();
    if (url.startsWith("redis://")) {
      factory.setUseSsl(true);
    }
    try {
      URI uri = new URI(url);
      factory.setHostName(uri.getHost());
      factory.setPort(uri.getPort());
      if (uri.getUserInfo() != null) {
        String password = uri.getUserInfo();
        int index = password.lastIndexOf(':');
        if (index >= 0) {
          password = password.substring(index + 1);
        }
        factory.setPassword(password);
      }
    } catch (URISyntaxException var6) {
      throw new IllegalArgumentException("Malformed 'spring.redis.url' " + url, var6);
    }
  }

  public LettuceConnectionFactory createLettuceConnectionFactory(RedisProperties redisProperties) {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(redisProperties.getHost());
    redisStandaloneConfiguration.setPort(redisProperties.getPort());
    redisStandaloneConfiguration.setPassword(redisProperties.getPassword());
    redisStandaloneConfiguration.setDatabase(redisProperties.getDatabase());

    LettuceClientConfiguration clientConfiguration =
        LettucePoolingClientConfiguration.builder()
            .clientResources(DefaultClientResources.create())
            .build();
    return new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfiguration);
  }

  protected final RedisClusterConfiguration getClusterConfiguration(RedisProperties properties) {
    if (properties.getCluster() == null) {
      return null;
    } else {
      Cluster clusterProperties = properties.getCluster();
      RedisClusterConfiguration config =
          new RedisClusterConfiguration(clusterProperties.getNodes());
      if (clusterProperties.getMaxRedirects() != null) {
        config.setMaxRedirects(clusterProperties.getMaxRedirects());
      }
      return config;
    }
  }

  private GenericObjectPoolConfig<?> getLettucePoolConfig(Pool pool) {
    GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
    poolConfig.setMaxIdle(pool.getMaxIdle());
    poolConfig.setMinIdle(pool.getMinIdle());
    poolConfig.setMaxTotal(pool.getMaxActive());
    poolConfig.setMaxWaitMillis(pool.getMaxWait().toMillis());
    return poolConfig;
  }
}
