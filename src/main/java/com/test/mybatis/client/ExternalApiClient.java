package com.test.mybatis.client;

import com.test.mybatis.client.config.CustomFeignConfig;
import com.test.mybatis.model.GdParseLocation;
import com.test.mybatis.model.TxParseLocation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

/**
 * api地址：
 *      @link {http://commit-openic.sf-express.com/#/apidoc}
 * 因为使用的是FastJson来处理Json数据
 * 所以此处将OpenFeign默认的Json解析器由Jackson转为FastJson
 */
@FeignClient(value = "sfApi", url = "https://commit-openic.sf-express.com", configuration = CustomFeignConfig.class, fallback = Void.class)
public interface ExternalApiClient {
    /**
     * 百度返回的response_header中的content-type=text/javascript;charset=utf-8，所以用string接收返回值
     * @param uri
     * @param address
     * @param output
     * @param ak
     * @return
     */
    @GetMapping(value = "/geocoding/v3/")
    String getBdLngLat(URI uri,
                       @RequestParam("address") String address,
                       @RequestParam(value = "output", defaultValue = "json") String output,
                       @RequestParam("city") String city,
                       @RequestParam("ak") String ak);

    @GetMapping(value = "/reverse_geocoding/v3/")
    String getBdAddress(URI uri,
                       @RequestParam("location") String location,
                       @RequestParam(value = "output", defaultValue = "json") String output,
                       @RequestParam("ak") String ak);

    /**
     * 高德地图解析
     * @param uri
     * @param address
     * @param output
     * @param key
     * @return
     */
    @GetMapping(value = "/v3/geocode/geo")
    GdParseLocation getGdLngLat(URI uri,
                                @RequestParam("key") String key,
                                @RequestParam("address") String address,
                                @RequestParam(value = "output", defaultValue = "json") String output,
                                @RequestParam(value = "city", required = false, defaultValue = "") String city,
                                @RequestParam(value = "batch", required = false, defaultValue = "false") String batch);

    @GetMapping(value = "/v3/geocode/regeo")
    String getGdAddress(URI uri,
                                @RequestParam("key") String key,
                                @RequestParam("location") String location,
                                @RequestParam(value = "output", defaultValue = "json") String output);

    /**
     * 腾讯地图解析
     * @param uri
     * @param address
     * @param output
     * @param key
     * @return
     */
    @GetMapping(value = "/ws/geocoder/v1/")
    TxParseLocation getTxMapLngLat(URI uri,
                                   @RequestParam("key") String key,
                                   @RequestParam("address") String address,
                                   @RequestParam(value = "output", defaultValue = "json") String output,
                                   @RequestParam(value = "region", required = false, defaultValue = "") String region);

    /**
     * 腾讯地图解析
     * @param uri
     * @param location
     * @param output
     * @param key
     * @return
     */
    @GetMapping(value = "/ws/geocoder/v1/")
    String getTxMapAddress(URI uri,
                                   @RequestParam("key") String key,
                                   @RequestParam("location") String location,
                                   @RequestParam(value = "output", defaultValue = "json") String output);

    @GetMapping("/v3/config/district")
    String getDistrictList(URI uri,
                           @RequestParam("key") String key,
                           @RequestParam("keywords") String keywords,
                           @RequestParam("subdistrict") String subdistrict,
                           @RequestParam("extensions") String extensions);
}
