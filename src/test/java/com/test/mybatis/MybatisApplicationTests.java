package com.test.mybatis;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import com.test.mybatis.client.ExternalApiClient;
import com.test.mybatis.config.ikanalyzer.CustomIKAnalyzer;
import com.test.mybatis.controller.AddressController;
import com.test.mybatis.model.AddressDetail;
import com.test.mybatis.model.BdBmsInfo;
import com.test.mybatis.model.GdBmsInfo;
import com.test.mybatis.model.api.BdLngLatResultVo;
import com.test.mybatis.model.api.BdParseAddress;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
class MybatisApplicationTests {
    RateLimiter limiter = RateLimiter.create(150);
    @Autowired
    private AddressController controller;
    @Autowired
    private ExternalApiClient client;
    @Autowired
    private CustomIKAnalyzer analyzer;

    @Test
    void contextLoads() throws Exception {
        String text = "福州市鼓楼区洪山镇西洪路捷报里9号捷报花园1号楼一单元302";
        List<String> list = analyzer.divide(text);
        System.out.println(list);

//        region();

//        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "10");
//        gdAddressParse();
//        bdAddressParse();
    }

    private void region() throws URISyntaxException {
        String list = client.getDistrictList(new URI("http://restapi.amap.com"), "2b222b483610149c5b6c99ccbe7a415e", "中国", "1", "base");

        JSONArray array = JSONObject.parseObject(list).getJSONArray("districts");

        TreeMap<Integer,String> provinceMap = new TreeMap<>(Comparator.naturalOrder());

        ((JSONObject) array.get(0)).getJSONArray("districts").forEach(
                e -> {
                    JSONObject e1 = (JSONObject) e;
                    provinceMap.put(e1.getInteger("adcode"), e1.getString("name"));
                }
        );
        System.out.println(provinceMap.size());
        provinceMap.entrySet().forEach(e -> System.out.println(e.getKey() + "-" + e.getValue()));

        TreeMap<Integer,String> cityMap = new TreeMap<>(Comparator.naturalOrder());
        provinceMap.values().forEach(e -> {
            String cities = null;
            try {
                cities = client.getDistrictList(new URI("http://restapi.amap.com"), "2b222b483610149c5b6c99ccbe7a415e", e, "1", "base");
            } catch (URISyntaxException uriSyntaxException) {
                uriSyntaxException.printStackTrace();
            }

            JSONArray cityArray = JSONObject.parseObject(cities).getJSONArray("districts");


            ((JSONObject) cityArray.get(0)).getJSONArray("districts").forEach(
                    element -> {
                        JSONObject e1 = (JSONObject) element;
                        cityMap.put(e1.getInteger("adcode"), e1.getString("name"));
                    }
            );
        });

        cityMap.entrySet().forEach(e -> System.out.println(e.getKey() + "-" + e.getValue()));
        System.out.println(cityMap.size());

        TreeMap<Integer,String> areaMap = new TreeMap<>(Comparator.naturalOrder());
        cityMap.keySet().stream().filter(e -> e < 810001).forEach(key -> {
            try {
                String cityName = cityMap.get(key);
                System.out.println(cityName);
                String areas = client.getDistrictList(new URI("http://restapi.amap.com"), "2b222b483610149c5b6c99ccbe7a415e", cityName, "1", "base");
                JSONArray areaArray = JSONObject.parseObject(areas).getJSONArray("districts");


                ((JSONObject) areaArray.get(0)).getJSONArray("districts").forEach(
                        element -> {
                            JSONObject e1 = (JSONObject) element;
                            areaMap.put(e1.getInteger("adcode"), cityName + "-" + e1.getString("name"));
                        }
                );
            } catch (URISyntaxException uriSyntaxException) {
                uriSyntaxException.printStackTrace();
            }

        });

        areaMap.entrySet().forEach(e -> System.out.println(e.getValue() + "-" + e.getKey()));
        System.out.println(areaMap.size());
    }

    private void bdAddressParse() throws Exception {
        IntStream.range(0, 20).forEach(index -> {
            try {
                String filename = "0909_" + index;
                List<String> tmp = FileUtils.readLines(new File("D:/address_parse/file/" + filename + ".txt"));
                List<BdBmsInfo> addressList = tmp.stream().map(e -> {
                    String[] split = e.split(",");
                    return new BdBmsInfo()
                            .setCity(split[0].replace("\"", ""))
                            .setDistrict(split[1].replace("\"", ""))
                            .setAddress(split[2].replace("\"", ""));
                }).collect(Collectors.toList());
                Map<String, List<BdBmsInfo>> map = addressList.stream().collect(Collectors.groupingBy(e -> e.getCity() + "-" + e.getDistrict() + "-" + e.getAddress()));
                System.out.println(map.size());

                List<CompletableFuture<Void>> futures = map.keySet().stream().map(e -> CompletableFuture.runAsync(() -> {
                    try {
                        double acquire = limiter.acquire(1);
                        String[] split = e.split("-");
                        String result = client.getBdLngLat(new URI("http://api.map.baidu.com"), split[2], "json", split[0], "59MyTZ40Lmr5Hyx6h0wiEpIAjUHu5aNv");
                        BdLngLatResultVo vo = JSONObject.parseObject(result, BdLngLatResultVo.class);

                        if(vo.getStatus() != 0) {
                            return;
                        }

                        String address = client.getBdAddress(new URI("http://api.map.baidu.com"), vo.getResult().getLocation().getLat() + "," + vo.getResult().getLocation().getLng(), "json", "59MyTZ40Lmr5Hyx6h0wiEpIAjUHu5aNv");

                        BdParseAddress readValue = JSONObject.parseObject(address, BdParseAddress.class);


                        BdParseAddress.ResultBean.AddressComponentBean bean = readValue.getResult().getAddressComponent();

                        map.get(e).forEach(value -> value.setAddress(split[2])
                                .setCity(bean.getCity())
                                .setBdAddress(readValue.getResult().getFormattedAddress()).setDistrict(bean.getDistrict())
                                .setBd(bean.getProvince() + "-" + bean.getCity() + "-" + bean.getDistrict()));
                    } catch (Exception jsonProcessingException) {
                        jsonProcessingException.printStackTrace();
                    }
                })).collect(Collectors.toList());

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();

                ExcelWriter build = EasyExcel.write(new File("D:/address_parse/excel/" + filename + ".xlsx"), BdBmsInfo.class).build();
                WriteSheet writeSheet = EasyExcel.writerSheet("模板").build();
                List<BdBmsInfo> list = map.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

                list.stream().forEach(e -> System.out.println(JSON.toJSONString(e)));

                build.write(list, writeSheet);
                build.finish();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

//        List<String> tmp = FileUtils.readLines(new File("D:/address_parse/0909_1_1.txt"));
//        List<BdBmsInfo> addressList = tmp.stream().map(e -> {
//            String[] split = e.split(",");
//            return new BdBmsInfo()
//                    .setCity(split[0].replace("\"", ""))
//                    .setDistrict(split[1].replace("\"", ""))
//                    .setAddress(split[2].replace("\"", ""));
//        }).collect(Collectors.toList());
//
//
//        Map<String, List<BdBmsInfo>> map = addressList.stream().collect(Collectors.groupingBy(e -> e.getCity() + "-" + e.getDistrict() + "-" + e.getAddress()));
//        System.out.println(map.size());
//
//
//        List<CompletableFuture<Void>> futures = map.keySet().stream().map(e -> CompletableFuture.runAsync(() -> {
//            try {
//                double acquire = limiter.acquire(1);
//                System.out.println(acquire);
//                String[] split = e.split("-");
//                String result = client.getBdLngLat(new URI("http://api.map.baidu.com"), split[2], "json", split[0], "59MyTZ40Lmr5Hyx6h0wiEpIAjUHu5aNv");
//                BdLngLatResultVo vo = JSONObject.parseObject(result, BdLngLatResultVo.class);
//
//                if(vo.getStatus() != 2) {
//                    return;
//                }
//
//                String address = client.getBdAddress(new URI("http://api.map.baidu.com"), vo.getResult().getLocation().getLat() + "," + vo.getResult().getLocation().getLng(), "json", "59MyTZ40Lmr5Hyx6h0wiEpIAjUHu5aNv");
//
//                BdParseAddress readValue = JSONObject.parseObject(address, BdParseAddress.class);
//
//
//                BdParseAddress.ResultBean.AddressComponentBean bean = readValue.getResult().getAddressComponent();
//
//                map.get(e).forEach(value -> value
//                        .setAddress(split[2])
//                        .setCity(bean.getCity())
//                        .setBdAddress(readValue.getResult().getFormattedAddress()).setDistrict(bean.getDistrict())
//                        .setBd(bean.getProvince() + "-" + bean.getCity() + "-" + bean.getDistrict()));
//            } catch (Exception jsonProcessingException) {
//                jsonProcessingException.printStackTrace();
//            }
//        })).collect(Collectors.toList());
//
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
//
//        ExcelWriter build = EasyExcel.write(new File("d:/bd_24.xlsx"), BdBmsInfo.class).build();
//        WriteSheet writeSheet = EasyExcel.writerSheet("模板").build();
//        List<BdBmsInfo> list = map.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
//
//        list.stream().forEach(e -> System.out.println(JSON.toJSONString(e)));
//
//        build.write(list, writeSheet);
//        build.finish();
    }

    private void gdAddressParse() {
        try {
            List<String> tmp = FileUtils.readLines(new File("D:/0909.txt"));

            List<GdBmsInfo> addressList = tmp.stream().map(e -> {
                String[] split = e.split(",");
                return new GdBmsInfo()
                        .setCity(split[0].replace("\"", ""))
                        .setDistrict(split[1].replace("\"", ""))
                        .setAddress(split[2].replace("\"", ""));
            }).collect(Collectors.toList());


            Map<String, List<GdBmsInfo>> map = addressList.stream().collect(Collectors.groupingBy(e -> e.getCity() + e.getDistrict() + e.getAddress()));
            System.out.println(map.size());

            List<AddressDetail> address = controller.getAddressDetailListByAddress(new ArrayList<>(map.keySet()));
            address.forEach(el -> System.out.println(JSON.toJSONString(el)));
            address.forEach(el ->
                    map.getOrDefault(el.getRequestAddress(), (List<GdBmsInfo>) Collections.EMPTY_LIST)
                            .forEach(element ->
                                    element.setGd(el.getProvince() + "-" + el.getCity() + "-" + el.getDistrict())
                                            .setGdAddress(el.getFormattedAddress())
                                            .setRequestAddress(el.getRequestAddress()))
            );

            ExcelWriter build = EasyExcel.write(new File("d:/24.xlsx"), GdBmsInfo.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("模板").build();
            List<GdBmsInfo> list = map.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

            list.stream().forEach(e -> System.out.println(JSON.toJSONString(e)));

            build.write(list, writeSheet);
            build.finish();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
