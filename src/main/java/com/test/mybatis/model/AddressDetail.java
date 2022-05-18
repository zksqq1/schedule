package com.test.mybatis.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * 地址详细信息
 */
@Data
@Accessors(chain = true)
public class AddressDetail {
    private static final String MUNICIPALITY = "北京市，天津市，上海市，重庆市";

    /**
     * 坐标类型：百度
     */
    public static final int LBS_TYPE_BD = 1;
    /**
     * 坐标类型：高德
     */
    public static final int LBS_TYPE_GD = 2;
    /**
     * 坐标类型：腾讯
     */
    public static final int LBS_TYPE_TX = 3;

    @ApiModelProperty("经度")
    private String lng;
    @ApiModelProperty("纬度")
    private String lat;
    @ApiModelProperty("坐标类型：1百度坐标，2高德坐标, 3腾讯")
    private Integer lbsType;
    @ApiModelProperty("结构化地址信息：省份＋城市＋区县＋城镇＋乡村＋街道＋门牌号码")
    private String formattedAddress;
    @ApiModelProperty("国家")
    private String country;
    @ApiModelProperty("地址所在的省份名")
    private String province;
    @ApiModelProperty("地址所在的城市名")
    private String city;
    @ApiModelProperty("地址所在的区")
    private String district;
    private String requestAddress;

    public static AddressDetail transferFromGd(GdParseLocation.GeoCodesBean bean) {
        String[] location;
        if (bean.getLngLat().contains(",")) {
            location = bean.getLngLat().split(",");
        } else {
            location = new String[]{"", "" };
        }

        String city = Objects.equals(bean.getCity(), "[]") || Objects.equals(bean.getCity(), "") ?
                (MUNICIPALITY.contains(bean.getProvince()) ? bean.getProvince() : bean.getDistrict()) :
                bean.getCity();

        return new AddressDetail()
                .setLng(location[0])
                .setLat(location[1])
                .setLbsType(LBS_TYPE_GD)
                .setCountry(bean.getCountry())
                .setProvince(bean.getProvince())
                .setCity(city)
                .setDistrict(Objects.equals(bean.getDistrict(), "[]") ? "" : bean.getDistrict())
                .setFormattedAddress(bean.getFormattedAddress());
    }
}
