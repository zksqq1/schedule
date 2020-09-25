package com.test.mybatis.model;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 高德地址解析经纬度响应信息
 * @link {https://lbs.amap.com/api/webservice/guide/api/georegeo#limit}
 */
@NoArgsConstructor
@Data
public class GdParseLocation {
    @ApiModelProperty("返回结果状态值:0 表示请求失败；1 表示请求成功")
    private String status;
    @ApiModelProperty("返回状态说明,当 status 为 0 时，info 会返回具体错误原因，否则返回“OK”。{https://lbs.amap.com/api/webservice/guide/tools/info}")
    private String info;
    @JSONField(name = "infocode")
    @ApiModelProperty("返回状态码：10000正常，与info一一对应")
    private String infoCode;
    @ApiModelProperty("返回结果数目")
    private String count;
    @JSONField(name = "geocodes")
    @ApiModelProperty("结果对象列表")
    private List<GeoCodesBean> geoCodes;

    @NoArgsConstructor
    @Data
    public static class GeoCodesBean {
        @ApiModelProperty("结构化地址信息：省份＋城市＋区县＋城镇＋乡村＋街道＋门牌号码")
        private String formattedAddress;
        @ApiModelProperty("国家")
        private String country;
        @ApiModelProperty("地址所在的省份名")
        private String province;
        @JSONField(name = "citycode")
        @ApiModelProperty("城市编码")
        private String cityCode;
        @ApiModelProperty("地址所在的城市名")
        private String city;
        @ApiModelProperty("地址所在的区")
        private String district;
        private NeighborhoodBean neighborhood;
        private BuildingBean building;
        @JSONField(name = "adcode")
        @ApiModelProperty("区域编码")
        private String adCode;
        @ApiModelProperty("坐标点：经度，纬度")
        @JSONField(name = "location")
        private String lngLat;
        @ApiModelProperty("level")
        private String level;
        private List<String> township;
        @ApiModelProperty("街道")
        private List<String> street;
        @ApiModelProperty("门牌")
        private List<String> number;

        @NoArgsConstructor
        @Data
        public static class NeighborhoodBean {
            private List<String> name;
            private List<String> type;
        }

        @NoArgsConstructor
        @Data
        public static class BuildingBean {
            private List<String> name;
            private List<String> type;
        }
    }
}
