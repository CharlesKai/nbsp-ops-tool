package com.nbsp.ops.util;

import com.alibaba.fastjson.JSONObject;

/**
 * @author charlesYan
 * @description:
 * @date 2024年07月24日
 */
public class JsonUtil {

  private JSONObject jsonObject;

  private JsonUtil() {
    this.jsonObject = new JSONObject();
  }

  public static JsonUtil create() {
    return new JsonUtil();
  }

  public JsonUtil put(String key, Object value) {
    this.jsonObject.put(key, value);
    return this;
  }

  public JSONObject build() {
    return this.jsonObject;
  }
}
