package com.nbsp.ops.controller;

import com.alibaba.fastjson.JSONObject;
import com.nbsp.ops.config.annotation.RequiresPermissions;
import com.nbsp.ops.service.ArticleService;
import com.nbsp.ops.util.CommonUtil;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author: CharlesYan
 * @description: 文章相关Controller
 * @date: 2017/10/24 16:04
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

  @Autowired private ArticleService articleService;

  /** 查询文章列表 */
  @RequiresPermissions("article:list")
  @GetMapping("/listArticle")
  public JSONObject listArticle(HttpServletRequest request) {
    return articleService.listArticle(CommonUtil.request2Json(request));
  }

  /** 新增文章 */
  @RequiresPermissions("article:add")
  @PostMapping("/addArticle")
  public JSONObject addArticle(@RequestBody JSONObject requestJson) {
    CommonUtil.hasAllRequired(requestJson, "content");
    return articleService.addArticle(requestJson);
  }

  /** 修改文章 */
  @RequiresPermissions("article:update")
  @PostMapping("/updateArticle")
  public JSONObject updateArticle(@RequestBody JSONObject requestJson) {
    CommonUtil.hasAllRequired(requestJson, "id,content");
    return articleService.updateArticle(requestJson);
  }
}
