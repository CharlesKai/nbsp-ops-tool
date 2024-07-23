package com.nbsp.ops.controller;

import com.alibaba.fastjson.JSONObject;
import com.nbsp.ops.config.annotation.RequiresPermissions;
import com.nbsp.ops.service.RedisService;
import com.nbsp.ops.util.CommonUtil;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: CharlesYan
 * @description Redis相关controller
 * @date: 2024/7/23
 */
@RestController
@RequestMapping("/redis")
@Slf4j
public class RedisController {

  @Autowired private RedisService redisService;

  /** 查询数据列表 */
  @RequiresPermissions("redis:list")
  @GetMapping("/list")
  public JSONObject listUser(HttpServletRequest request) {
    JSONObject jsonObject = CommonUtil.request2Json(request);
    log.info("查询入参：{}", jsonObject.toJSONString());
    JSONObject result = redisService.listKeysWithValues(jsonObject);
    log.info("响应结果：{}", result.toJSONString());
    return result;
  }
}
