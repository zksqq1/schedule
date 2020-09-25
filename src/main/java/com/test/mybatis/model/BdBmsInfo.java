package com.test.mybatis.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 *
 */
@Data
@Accessors(chain = true)
@ToString
@NoArgsConstructor
public class BdBmsInfo {
    @ExcelProperty("市")
    private String city;
    @ExcelProperty("区")
    private String district;
    @ExcelProperty("详细地址")
    private String address;
    @ExcelProperty("请求时的地址")
    private String requestAddress;
    @ExcelProperty("百度经纬度解析省市区")
    private String bd;
    @ExcelProperty("百度经纬度解析格式化的地址")
    private String bdAddress;
}
