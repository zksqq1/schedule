package com.test.mybatis.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 *
 */
@Data
@Accessors(chain = true)
@ToString
public class GdBmsInfo {
    @ExcelProperty("市")
    private String city;
    @ExcelProperty("区")
    private String district;
    @ExcelProperty("详细地址")
    private String address;
    @ExcelProperty("请求时的地址")
    private String requestAddress;
    @ExcelProperty("高德经纬度解析省市区")
    private String gd;
    @ExcelProperty("高德经纬度解析格式化的地址")
    private String gdAddress;
}
