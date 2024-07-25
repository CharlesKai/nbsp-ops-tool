package com.nbsp.ops.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * @author charlesYan
 * @description: Redis库信息实体类
 * @date 2024年07月24日
 */
@Data
@Builder
public class RedisExcelDTO implements Serializable {

  private static final long serialVersionUID = 5213628505480679486L;

  @ExcelProperty(value = "主机", index = 0)
  @ColumnWidth(16)
  private String host;

  /** 键 */
  @ExcelProperty(value = "键", index = 1)
  @ColumnWidth(16)
  private String key;

  /** 值 */
  @ExcelProperty(value = "值", index = 2)
  @ColumnWidth(30)
  private String value;

  /** 数据类型 */
  @ExcelProperty(value = "数据类型", index = 3)
  @ColumnWidth(15)
  private String type;

  /** 库号 */
  @ExcelProperty(value = "库号", index = 4)
  @ColumnWidth(12)
  private String databaseIndex;
}
