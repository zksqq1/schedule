package com.test.mybatis.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 */
@NoArgsConstructor
@Data
public class TxParseLocation {

    /**
     * status : 0
     * message : query ok
     * result : {"title":"海淀西大街74号","location":{"lng":116.307015,"lat":39.982915},"ad_info":{"adcode":"110108"},"address_components":{"province":"北京市","city":"北京市","district":"海淀区","street":"海淀西大街","street_number":"74"},"similarity":0.8,"deviation":1000,"reliability":7,"level":9}
     */
    @ApiModelProperty("状态码，0为正常，310请求参数信息有误，311Key格式错误，306请求有护持信息请检查字符串，110请求来源未被授权")
    private Integer status;
    @ApiModelProperty("状态说明")
    private String message;
    @ApiModelProperty("地址解析结果")
    private ResultBean result;

    @NoArgsConstructor
    @Data
    public static class ResultBean {
        @ApiModelProperty("地址解析结果")
        private String title;
        @ApiModelProperty("经纬度信息")
        private LocationBean location;
        @ApiModelProperty("行政区域编码信息")
        private AdInfoBean adInfo;
        @ApiModelProperty("解析后的地址部件")
        private AddressComponentsBean addressComponents;
        @ApiModelProperty("即将下线，由reliability代替")
        private Double similarity;
        @ApiModelProperty("即将下线，由reliability代替")
        private int deviation;
        @ApiModelProperty("可信度参考：值范围 1 <低可信> - 10 <高可信>\n" +
                "我们根据用户输入地址的准确程度，在解析过程中，将解析结果的可信度(质量)，由低到高，" +
                "分为1 - 10级，该值>=7时，解析结果较为准确，" +
                "<7时，会存各类不可靠因素，开发者可根据自己的实际使用场景，对于解析质量的实际要求，进行参考。")
        private int reliability;
        @ApiModelProperty("解析精度级别，分为11个级别，一般>=9即可采用（定位到点，精度较高） 也可根据实际业务需求自行调整。")
        /**
         * 1	城市
         * 2	区、县
         * 3	乡镇、街道
         * 4	村、社区
         * 5	开发区
         * 6	热点区域、商圈
         * 7	道路
         * 8	道路附属点：交叉口、收费站、出入口等
         * 9	门址
         * 10	小区、大厦
         * 11	POI点
         */
        private Integer level;

        @NoArgsConstructor
        @Data
        public static class LocationBean {
            @ApiModelProperty("经度")
            private Double lng;
            @ApiModelProperty("纬度")
            private Double lat;
        }

        @NoArgsConstructor
        @Data
        public static class AdInfoBean {
            @ApiModelProperty("行政区域编码信息")
            private String adcode;
        }

        @NoArgsConstructor
        @Data
        public static class AddressComponentsBean {
            @ApiModelProperty("省份")
            private String province;
            @ApiModelProperty("城市")
            private String city;
            @ApiModelProperty("区县")
            private String district;
            @ApiModelProperty("街道")
            private String street;
            @ApiModelProperty("门牌号")
            private String streetNumber;
        }
    }
}
