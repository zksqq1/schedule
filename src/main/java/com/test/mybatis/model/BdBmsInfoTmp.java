package com.test.mybatis.model;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 *
 */
@Data
@ToString
public class BdBmsInfoTmp {
    @ExcelProperty("市")
    @ApiModelProperty("市")
    private String city;
    @ExcelProperty("区")
    @ApiModelProperty("区")
    private String district;
    @ExcelProperty("详细地址")
    @ApiModelProperty("详细地址")
    private String address;
    @ExcelProperty("请求时的地址")
    @ApiModelProperty("请求时的地址")
    private String requestAddress;
    @ExcelProperty("百度经纬度解析省市区")
    @ApiModelProperty("百度经纬度解析省市区")
    private String bd;
    @ExcelProperty("百度经纬度解析格式化的地址")
    @ApiModelProperty("百度经纬度解析格式化的地址")
    private String bdAddress;
}
