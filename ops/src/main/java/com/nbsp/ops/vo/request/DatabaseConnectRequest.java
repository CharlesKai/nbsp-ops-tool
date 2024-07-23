package com.nbsp.ops.vo.request;

import java.util.Map;

/**
 * @author charlesYan
 * @description: 数据库连接请求
 * @date 2024年07月22日
 */
public class DatabaseConnectRequest {

  /** 数据类型 */
  private String type;
  /** 用户名称 */
  private String user;
  /** 密码 */
  private String password;
  /** Oracle 就是SID */
  private String databaseName;
  /** 数据库模式， Mysql 该字段无用处 */
  private String schema;
  /** 主机 + 端口 */
  private String host;
  /** 是否采用ssl */
  private boolean useSSL;
  /** 数据库对应版本 */
  private String version;
  /** 其他参数 */
  private Map<String, String> extraParams;
}
