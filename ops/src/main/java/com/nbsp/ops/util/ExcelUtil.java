package com.nbsp.ops.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.springframework.http.HttpHeaders;

@Slf4j
public class ExcelUtil {
  /** 私有化构造方法 */
  private ExcelUtil() {}

  /**
   * 导出excel
   *
   * @param fileName excel文件名称
   * @param sheetName excel sheet名称
   * @param list 数据
   * @param clazz
   * @param response
   */
  public static void exportExcel(
      String fileName, String sheetName, List list, Class clazz, HttpServletResponse response)
      throws IOException {
    // 替换空格（+）为 %20
    String encodedFileName =
        URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
    String disposition = "attachment; filename=" + encodedFileName + ExcelTypeEnum.XLSX.getValue();
    response.setContentType("application/vnd.ms-excel");
    response.setCharacterEncoding("utf8");
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, disposition);
    try (ServletOutputStream outputStream = response.getOutputStream()) {
      EasyExcel.write(outputStream)
          .head(clazz)
          .excelType(ExcelTypeEnum.XLSX)
          .sheet(sheetName)
          .doWrite(list);
      outputStream.flush();
    }
  }

  /**
   * 导出Excel(07版.xlsx)到指定路径下
   *
   * @param path 路径
   * @param excelName Excel名称
   * @param sheetName sheet页名称
   * @param clazz Excel要转换的类型
   * @param data 要导出的数据
   */
  public static void export2File(
      String path, String excelName, String sheetName, Class clazz, List data) {
    String fileName = path.concat(excelName).concat(ExcelTypeEnum.XLSX.getValue());
    EasyExcel.write(fileName, clazz).sheet(sheetName).doWrite(data);
  }

  /**
   * 导出Excel(07版.xlsx)到web
   *
   * @param response 响应
   * @param excelName Excel名称
   * @param sheetName sheet页名称
   * @param clazz Excel要转换的类型
   * @param data 要导出的数据
   * @throws Exception
   */
  public static void export2Web(
      HttpServletResponse response, String excelName, String sheetName, Class clazz, List data)
      throws Exception {
    response.setContentType("application/vnd.ms-excel");
    response.setCharacterEncoding("utf-8");
    // 这里URLEncoder.encode可以防止中文乱码
    excelName = URLEncoder.encode(excelName, "UTF-8");
    response.setHeader(
        "Content-disposition", "attachment;filename=" + excelName + ExcelTypeEnum.XLSX.getValue());
    EasyExcel.write(response.getOutputStream(), clazz).sheet(sheetName).doWrite(data);
  }

  /**
   * 将指定位置指定名称的Excel导出到web
   *
   * @param response 响应
   * @param path 文件路径
   * @param excelName 文件名称
   * @throws IOException
   */
  public static void export2Web4File(HttpServletResponse response, String path, String excelName)
      throws IOException {
    File file = new File(path.concat(excelName).concat(ExcelTypeEnum.XLSX.getValue()));
    if (!file.exists()) {
      log.error("文件{}不存在", path.concat(excelName));
    }
    response.setContentType("application/vnd.ms-excel");
    response.setCharacterEncoding("utf-8");
    // 这里URLEncoder.encode可以防止中文乱码
    excelName = URLEncoder.encode(excelName, "UTF-8");
    response.setHeader(
        "Content-disposition", "attachment;filename=" + excelName + ExcelTypeEnum.XLSX.getValue());
    try (FileInputStream in = new FileInputStream(file);
        ServletOutputStream out = response.getOutputStream(); ) {
      IOUtils.copy(in, out);
    }
  }
}
