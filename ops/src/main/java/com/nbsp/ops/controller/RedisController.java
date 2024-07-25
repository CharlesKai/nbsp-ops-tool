package com.nbsp.ops.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nbsp.ops.config.annotation.RequiresPermissions;
import com.nbsp.ops.dto.excel.RedisExcelDTO;
import com.nbsp.ops.service.RedisService;
import com.nbsp.ops.util.CommonUtil;
import com.nbsp.ops.util.ExcelUtil;
import com.nbsp.ops.util.constants.Constants;
import com.nbsp.ops.util.constants.ErrorEnum;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  public JSONObject listKeyValue(HttpServletRequest request) {
    JSONObject jsonObject = CommonUtil.request2Json(request);
    CommonUtil.fillPageParam(jsonObject);
    log.info("查询入参：{}", jsonObject.toJSONString());
    JSONObject result = redisService.listKeysWithValues(jsonObject);
    log.info("响应结果：{}", result.toJSONString());
    return result;
  }

  /** 导出数据列表 */
  @RequiresPermissions("redis:export")
  @GetMapping("/export")
  public JSONObject exportKeyValue(
      HttpServletResponse response, @RequestBody JSONObject requestJson) {
    CommonUtil.hasAllRequired(requestJson, "excelName");
    String excelName = requestJson.getString("excelName");
    log.info("查询入参：{}", requestJson);
    JSONObject result = redisService.listAllKeysWithValues(requestJson);
    String code = result.getString("code");
    if (!Constants.SUCCESS_CODE.equals(code)) {
      return result;
    }
    JSONArray info = result.getJSONArray("info");
    if (info.isEmpty()) {
      return CommonUtil.errorJson(ErrorEnum.E_40011);
    }
    List<JSONObject> list = info.toJavaList(JSONObject.class);
    // 转换数据
    List<RedisExcelDTO> excelList =
        list.stream()
            .map(
                item ->
                    RedisExcelDTO.builder()
                        .key(item.getString("key"))
                        .value(item.getString("value"))
                        .type(item.getString("type"))
                        .host(item.getString("host"))
                        .databaseIndex(item.getString("database"))
                        .build())
            .collect(Collectors.toList());
    // 调用工具类导出
    try {
      ExcelUtil.exportExcel("用户数据", "redis", excelList, RedisExcelDTO.class, response);
    } catch (IOException e) {
      log.error("数据导出 {} 异常：", excelName, e);
      return CommonUtil.errorJson(ErrorEnum.E_400);
    }
    return CommonUtil.successJson();
  }
}
