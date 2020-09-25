package com.test.mybatis.model;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 *
 */
@Data
public class LocationInfo {
    @ExcelProperty("单号")
    private String code;
    @ExcelProperty("详细地址")
    private String address;
    @ExcelProperty("省")
    private String province;
    @ExcelProperty("市")
    private String city;
    @ExcelProperty("区")
    private String district;
    @ExcelProperty("百度地址解析省市区")
    private String bd;
    @ExcelProperty("百度地址解析格式化的地址")
    private String bdAddress;
    @ExcelProperty("高德经纬度解析省市区")
    private String gd;
    @ExcelProperty("高德经纬度解析格式化的地址")
    private String gdAddress;
    @ExcelProperty("高德地址解析省市区")
    private String gdAddressParsePcs;
    @ExcelProperty("高德地址解析格式化地址")
    private String gdAddressParseAddress;
    @ExcelProperty("腾讯经纬度解析省市区")
    private String tx;
    @ExcelProperty("腾讯地址解析省市区")
    private String txAddressParsePcs;
    @ExcelProperty("腾讯地址解析格式化地址")
    private String txAddress;
}
