package com.nbsp.ops.service;

import com.alibaba.fastjson.JSONObject;
import com.nbsp.ops.config.redis.DynamicRedisConfig;
import com.nbsp.ops.util.CommonUtil;
import com.nbsp.ops.util.RedisUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

/**
 * @author charlesYan
 * @description: redis 服务
 * @date 2024年07月22日
 */
@Service
@Slf4j
public class RedisService {

  @Autowired private DynamicRedisConfig redisConfig;

  /**
   * 数据库列表
   *
   * @param jsonObject
   * @return com.alibaba.fastjson.JSONObject
   */
  public JSONObject listDatabase(JSONObject jsonObject) {
    CommonUtil.fillPageParam(jsonObject);
    RedisTemplate<String, String> redisTemplate =
        redisConfig.createRedisTemplate(
            jsonObject.getString("host"),
            jsonObject.getIntValue("port"),
            jsonObject.getString("password"),
            -1);
    RedisUtil redisUtil = new RedisUtil(redisTemplate);
    return null;
  }

  /**
   * 数据列表
   *
   * @param jsonObject
   * @return com.alibaba.fastjson.JSONObject
   */
  public JSONObject listKeysWithValues(JSONObject jsonObject) {
    CommonUtil.fillPageParam(jsonObject);
    int offSet = jsonObject.getIntValue("offSet");
    int limit = jsonObject.getIntValue("pageRow");
    String host = jsonObject.getString("host");
    int port = jsonObject.getIntValue("port");
    String password = jsonObject.getString("password");
    int database = jsonObject.getIntValue("database");

    RedisTemplate<String, String> redisTemplate =
        redisConfig.createRedisTemplate(host, port, password, database);
    long count = countKeys(redisTemplate, database);
    Map<String, Object> dataMap = listKeysWithValues(redisTemplate, database, offSet, limit);
    // 将Map转换为List<JSONObject>
    List<JSONObject> list =
        dataMap.entrySet().stream()
            .map(
                entry -> {
                  JSONObject item = new JSONObject();
                  item.put("key", entry.getKey());
                  item.put("value", entry.getValue());
                  return item;
                })
            .collect(Collectors.toList());

    return CommonUtil.successPage(jsonObject, list, Math.toIntExact(count));
  }

  /**
   * 分页查询redis库下键值列表
   *
   * <p>scan方式
   *
   * @param redisTemplate
   * @param database
   * @param start
   * @param size
   * @return java.util.Map<java.lang.String, java.lang.Object>
   */
  private Map<String, Object> listKeysWithValues(
      RedisTemplate<String, String> redisTemplate, int database, int start, int size) {
    // 待缓存连接池，是否需要destroy
    RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
    RedisConnection connection = connectionFactory.getConnection();
    connection.select(database);
    ScanOptions options = ScanOptions.scanOptions().count(size).build();
    Map<String, Object> result = new LinkedHashMap<>();
    int index = 0;
    int end = start + size;
    // Try-with-resources for automatic resource management
    try (Cursor<byte[]> cursor = connection.scan(options)) {
      while (cursor.hasNext()) {
        byte[] key = cursor.next();
        if (index >= start && index < end) {
          result.put(new String(key, StandardCharsets.UTF_8), getValueByKey(connection, key));
        }
        index++;
      }
      return result;
    } catch (IOException e) {
      log.error("Scan redis keys of database {} error: ", database, e);
    } finally {
      connection.close();
    }
    return result;
  }

  /**
   * 分页查询redis库下键值列表
   *
   * @param redisTemplate redis客户端
   * @param database 数据库号
   * @param pattern 匹配规则
   * @param start 起始位置
   * @param size 数量
   * @return java.util.Map<java.lang.String, java.lang.Object>
   */
  private Map<String, Object> listKeysWithValues(
      RedisTemplate<String, String> redisTemplate,
      int database,
      String pattern,
      int start,
      int size) {
    ScanOptions options = ScanOptions.scanOptions().match(pattern).count(size).build();
    RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
    try (Cursor<byte[]> cursor = connection.scan(options)) {
      List<String> keys = new ArrayList<>();
      cursor.forEachRemaining(key -> keys.add(new String(key)));

      int end = Math.min(start + size, keys.size());

      if (start >= keys.size()) {
        return Collections.emptyMap();
      }

      List<String> pageKeys = keys.subList(start, end);
      List<String> values = redisTemplate.opsForValue().multiGet(pageKeys);

      return pageKeys.stream()
          .collect(Collectors.toMap(key -> key, key -> values.get(pageKeys.indexOf(key))));
    } catch (IOException e) {
      log.error("Scan Redis keys of database {} error: ", database, e);
    } finally {
      connection.close();
    }
    return Collections.emptyMap();
  }

  /**
   * 获取指定数据库的键数量
   *
   * @param database 数据库索引
   * @return 键的数量
   */
  private long countKeys(RedisTemplate<String, String> redisTemplate, int database) {
    try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
      // 切换到指定的数据库
      connection.select(database);
      // 获取键的数量
      return connection.dbSize();
    }
  }

  /**
   * 全量查询redis库下所有键值列表
   *
   * @param redisTemplate
   * @param database
   * @return java.util.Map<java.lang.String, java.lang.Object>
   */
  private Map<String, Object> listAllKeysWithValues(
      RedisTemplate<String, String> redisTemplate, int database) {
    Set<String> keys = redisTemplate.keys("*");
    Map<String, Object> result = new LinkedHashMap<>();
    if (keys != null) {
      for (String key : keys) {
        Object value = getValueByKey(redisTemplate, key);
        result.put(key, value);
      }
    }
    return result;
  }

  private Object getValueByKey(RedisTemplate<String, String> redisTemplate, String key) {
    String type = redisTemplate.type(key).code();
    switch (type) {
      case "string":
        return redisTemplate.opsForValue().get(key);
      case "list":
        return redisTemplate.opsForList().range(key, 0, -1);
      case "set":
        return redisTemplate.opsForSet().members(key);
      case "zset":
        return redisTemplate.opsForZSet().range(key, 0, -1);
      case "hash":
        return redisTemplate.opsForHash().entries(key);
      default:
        log.warn("The type of key {} is unknown", key);
        return null;
    }
  }

  private Object getValueByKey(RedisConnection connection, byte[] key) {
    String type = connection.type(key).name();
    switch (type) {
      case "string":
        return new String(connection.get(key), StandardCharsets.UTF_8);
      case "list":
        return connection.lRange(key, 0, -1);
      case "set":
        return connection.sMembers(key);
      case "zset":
        return connection.zRange(key, 0, -1);
      case "hash":
        return connection.hGetAll(key);
      default:
        log.warn("The type of key {} is unknown", new String(key, StandardCharsets.UTF_8));
        return null;
    }
  }
}
