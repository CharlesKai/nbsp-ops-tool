package com.nbsp.ops.config.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nbsp.ops.dto.excel.RedisExcelDTO;
import com.nbsp.ops.service.RedisService;
import com.nbsp.ops.util.JsonUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author charlesYan
 * @description:
 * @date 2024年07月24日
 */
@Slf4j
public class RedisExcelListener implements ReadListener<RedisExcelDTO> {

  /** 每隔3000条数据存储数据库，然后清理List,方便内存回收 */
  private static final int BATCH_COUNT = 3000;

  /** 缓存的数据 */
  private List<RedisExcelDTO> cacheList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

  private RedisService redisService;

  public RedisExcelListener(RedisService redisService) {
    this.redisService = redisService;
  }

  @Override
  public void invoke(RedisExcelDTO redis, AnalysisContext analysisContext) {

    log.info("解析到一条redis数据:{}", JSON.toJSONString(redis));
    cacheList.add(redis);
    // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
    if (cacheList.size() >= BATCH_COUNT) {
      saveData();
      cacheList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    }
  }

  @Override
  public void doAfterAllAnalysed(AnalysisContext analysisContext) {
    // 这里也要保存数据，确保最后遗留的数据也存储到数据库
    saveData();
    log.info("所有数据解析完成！");
  }

  /** 保存数据 */
  private void saveData() {
    log.info("共{}条数据，存储数据库开始！", cacheList.size());
    if (CollectionUtils.isNotEmpty(cacheList)) {
      List<JSONObject> userList =
          cacheList.stream()
              .map(
                  item ->
                      JsonUtil.create()
                          .put("database", item.getDatabaseIndex())
                          .put("key", item.getKey())
                          .put("value", item.getValue())
                          .build())
              .collect(Collectors.toList());
      redisService.saveBatch(userList);
    }
    log.info("存储数据库成功！");
  }
}
