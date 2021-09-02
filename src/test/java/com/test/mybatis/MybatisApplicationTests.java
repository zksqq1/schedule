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
import com.test.mybatis.entity.*;
import com.test.mybatis.entity.address.County;
import com.test.mybatis.entity.address.CountyWrapper;
import com.test.mybatis.mapper.AddressMapper;
import com.test.mybatis.model.AddressDetail;
import com.test.mybatis.model.BdBmsInfo;
import com.test.mybatis.model.GdBmsInfo;
import com.test.mybatis.model.address.ModifyInfo;
import com.test.mybatis.model.api.BdLngLatResultVo;
import com.test.mybatis.model.api.BdParseAddress;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
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
    @Autowired
    private AddressMapper addressMapper;

    @Test
    void contextLoads() throws Exception {
//        saveStandardAddress();
//        incrementUpdateCounty("http://www.mca.gov.cn///article/sj/xzqh/2020/2020/2020072805002.html");

//        fetchCountyData();


        //1oms。2sec。3tms
//        HashMap<String, AreaMapping> omsSavedMap = new HashMap<>();
//        areaMapping("D:/sec-oms.csv", 2, "D:/sec-oms_not.csv", omsSavedMap);
//        areaMapping("D:/tms-oms.csv", 3, "D:/tms-oms_not.csv", omsSavedMap);

        countyMapping();

//        region();


//        List<String> lines = FileUtils.readLines(new File("D:\\0909.txt"));
//
//        List<String> result = lines.stream().map(e -> {
//            e = e.replace("\",\"", "");
//            try {
//                return e + "\t\t" + analyzer.divide(e).toString();
//            } catch (IOException ioException) {
//                ioException.printStackTrace();
//            }
//            return "";
//        }).collect(Collectors.toList());
//
//        FileUtils.writeLines(new File("D:\\1010.txt"), result);

//        String text = "福州市鼓楼区洪山镇西洪路捷报里9号捷报花园1号楼一单元302";
//        List<String> list = analyzer.divide(text);
//        System.out.println(list);


//        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "10");
//        gdAddressParse();
//        bdAddressParse();
    }

    private void countyMapping() {
        try {
            List<CountyWrapper> wrappers =  addressMapper.selectCountyWrapper();
            List<County> countyList = addressMapper.selectCounty();
            List<String> list = FileUtils.readLines(new File("D:/oms_county.csv"));
            //key:areaCode-countyCode,value:data
            Map<String,String[]> omsMap = new HashMap<>();
            for (String s : list) {
                String[] split = s.replace("\"", "").split(",");
                omsMap.put(split[3] + "-" + split[5], split);
            }

            Map<String, County> map = countyList.stream().collect(Collectors.toMap(e -> e.getAreaCode() + "-" + e.getCountyName(), Function.identity()));

            List<MappingCounty> mappings = new ArrayList<>();

            for (int i = 0; i < wrappers.size(); i++) {
                CountyWrapper wrapper = wrappers.get(i);

                String mapKey = wrapper.getAreaCode() + "-" + wrapper.getCountyName();
                County county = map.get(mapKey);
                String[] split = omsMap.get(mapKey);

                MappingCounty mapping = new MappingCounty();
                mapping.setSystemCode(1);
                mapping.setStandardAreaCode(wrapper.getAreaCode());
                mapping.setStandardCountyCode(wrapper.getCountyCode());
                mapping.setStandardCityCode(wrapper.getCityCode());
                mapping.setStandardProvinceCode(wrapper.getProvinceCode());
                mapping.setIsUse(0);
                if(split != null && county != null) {
                    mapping.setSystemProvinceName(split[0]);
                    mapping.setSystemCityName(split[1]);
                    mapping.setSystemAreaName(split[2]);
                    mapping.setSystemCountyName(county.getCountyName());
                    mapping.setSystemCountyAliasName(county.getAliasName());
                    if(Objects.equals(county.getCountyCode(), wrapper.getCountyCode())) {
                        mapping.setIsUse(1);
                    } else {
                        mapping.setSystemCountyCode(county.getCountyCode());
                    }
                    map.remove(mapKey);
                    omsMap.remove(mapKey);
                }
                if(mappings.size() >= 200) {
                    addressMapper.insertMappingCounty(mappings);
                    mappings.clear();
                }
                mappings.add(mapping);
            }
            if(!CollectionUtils.isEmpty(mappings)) {
                addressMapper.insertMappingCounty(mappings);
            }

            if(map.size() > 0) {
                FileUtils.writeStringToFile(new File("D:/countyNoMapping.json"), JSON.toJSONString(map.values()));
                for (Map.Entry<String, County> entry : map.entrySet()) {
                    String key = entry.getKey();
                    String[] split = omsMap.get(key);
                    MappingCounty mapping = new MappingCounty();
                    County value = entry.getValue();
                    mapping.setSystemCountyCode(value.getCountyCode());
                    mapping.setSystemCountyName(value.getCountyName());
                    mapping.setSystemCountyAliasName(value.getAliasName());
                    mapping.setSystemCode(1);
                    if(split != null) {
                        mapping.setSystemProvinceName(split[0]);
                        mapping.setSystemCityName(split[1]);
                        mapping.setSystemAreaName(split[2]);
                    }
                    mapping.setIsUse(2);
                    if(mappings.size() >= 200) {
                        addressMapper.insertMappingCounty(mappings);
                        mappings.clear();
                    }
                    mappings.add(mapping);
                }
                if(!CollectionUtils.isEmpty(mappings)) {
                    addressMapper.insertMappingCounty(mappings);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchCountyData() {
        String provinceStr = "北京市\n" +
                "天津市\n" +
                "河北省\n" +
                "山西省\n" +
                "内蒙古自治区\n" +
                "辽宁省\n" +
                "吉林省\n" +
                "黑龙江省\n" +
                "上海市\n" +
                "江苏省\n" +
                "浙江省\n" +
                "安徽省\n" +
                "福建省\n" +
                "江西省\n" +
                "山东省\n" +
                "河南省\n" +
                "湖北省\n" +
                "湖南省\n" +
                "广东省\n" +
                "广西壮族自治区\n" +
                "海南省\n" +
                "重庆市\n" +
                "四川省\n" +
                "贵州省\n" +
                "云南省\n" +
                "西藏自治区\n" +
                "陕西省\n" +
                "甘肃省\n" +
                "青海省\n" +
                "宁夏回族自治区\n" +
                "新疆维吾尔自治区\n" +
                "香港特别行政区\n" +
                "澳门特别行政区";
        String[] strings = provinceStr.split("\n");
        Map<String, List<JSONObject>> map = null;
        try {
            String streetStr = FileUtils.readFileToString(new File("D:/streets.json"));
            map = JSONArray.parseArray(streetStr, JSONObject.class).stream().collect(Collectors.groupingBy(e -> e.getString("name")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, List<JSONObject>> finalMap = map;
        List<StandardCounty> blank = new ArrayList<>();
        List<Integer> countyCount = new ArrayList<>();
        Arrays.stream(strings).forEach(e -> {
            try {
                String districtList = client.getDistrictList(new URI("http://restapi.amap.com"), "2b222b483610149c5b6c99ccbe7a415e", e, "3", "base");
                JSONObject object = JSONObject.parseObject(districtList);
                JSONArray provinceArray = object.getJSONArray("districts");
                JSONObject province = provinceArray.getJSONObject(0);

                StandardProvince sp = new StandardProvince();
                sp.setProvinceCode(province.getString("adcode"));
                sp.setProvinceName(province.getString("name"));
                List<StandardCity> scs = new ArrayList<>();
                List<StandardArea> sas = new ArrayList<>();
                List<StandardCounty> cs = new ArrayList<>();
                recursion(province, sp, null, null, scs, sas, cs);
                countyCount.add(cs.size());

                for (int i = 0; i < cs.size(); i++) {
                    StandardCounty county = cs.get(i);

                    List<JSONObject> objects = Optional.ofNullable(finalMap.get(county.getCountyName())).orElse(Collections.emptyList());
                    String code = "";
                    if(objects.size() > 1) {
                        for (int i1 = 0; i1 < objects.size(); i1++) {
                            String areaCode = objects.get(i1).getString("areaCode");
                            if(areaCode.equals(county.getAreaCode())) {
                                code = objects.get(i1).getString("code");
                                break;
                            }
                        }
                    } else if(objects.size() == 1) {
                        code = objects.get(0).getString("code");
                    } else {
                        blank.add(county);
                        cs.remove(i);
                        i--;
                    }
                    county.setCountyCode(code);
                }
                System.out.println(cs.size());

//                System.out.println(JSON.toJSONString(sp));
//                System.out.println(JSON.toJSONString(scs));
//                System.out.println(JSON.toJSONString(sas));
//                System.out.println(JSON.toJSONString(cs));
                addressMapper.insertCity(scs);
                if(!CollectionUtils.isEmpty(sas)) {
                    addressMapper.insertArea(sas);
                }
                if(!CollectionUtils.isEmpty(cs)) {
                    addressMapper.insertCounty(cs);
                }
                if(!CollectionUtils.isEmpty(blank)) {
                    addressMapper.insertCounty(blank);
                    blank.clear();
                }
            } catch (URISyntaxException uriSyntaxException) {
                uriSyntaxException.printStackTrace();
            }
        });
//        try {
//            FileUtils.writeStringToFile(new File("D:/NoMapping.json"), JSON.toJSONString(blank));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        System.out.println(countyCount.stream().reduce(Integer::sum).get());
    }

    private void recursion(JSONObject jsonObject, StandardProvince province, StandardCity city, StandardArea area, List<StandardCity> scs, List<StandardArea> sas, List<StandardCounty> cs) {
        JSONArray array = jsonObject.getJSONArray("districts");
        if(array.size() == 1 && city == null && area == null) {
            array.getJSONObject(0).fluentPut("adcode", province.getProvinceCode()).fluentPut("name", province.getProvinceName());
        }
        JSONObject object;
        boolean isStreet = false;
        for (int i = 0; i < array.size(); i++) {
            object = array.getJSONObject(i);
            String level = object.getString("level");
            String name = object.getString("name");
            String code = object.getString("adcode");
            if(level.equals("street")) {
                isStreet = true;
                StandardCounty c = new StandardCounty();
                c.setCountyName(name);
                if(area == null && city != null) {
                    c.setAreaCode(city.getCityCode());
                } else {
                    c.setAreaCode(area.getAreaCode());
                }
                cs.add(c);
            } else if(level.equals("district")) {

                if(city == null) {
                    StandardCity sc = new StandardCity();
                    sc.setCityName(name);
                    sc.setCityCode(code);
                    sc.setProvinceCode(province.getProvinceCode());
                    scs.add(sc);
                } else {
                    StandardArea sa = new StandardArea();
                    sa.setAreaCode(code);
                    sa.setAreaName(name);
                    sa.setCityCode(city.getCityCode());
                    sas.add(sa);
                    recursion(object, province, city, sa, scs, sas, cs);
                }

            } else {
                StandardCity sc = new StandardCity();
                sc.setCityName(name);
                sc.setCityCode(code);
                sc.setProvinceCode(province.getProvinceCode());
                scs.add(sc);

                recursion(object, province, sc, null, scs, sas, cs);
            }
        }
        if(area == null && isStreet) {
            area = new StandardArea();
            area.setAreaName(city.getCityName());
            area.setAreaCode(city.getCityCode());
            area.setCityCode(city.getCityCode());
            sas.add(area);
        }
    }

    private void incrementUpdateProvinceCityArea() {

    }

    private void areaMapping(String filepath, Integer systemType, String notMappingFilepath, HashMap<String, MappingArea> omsMap) throws IOException {
        List<String> list = FileUtils.readLines(new File(filepath));
        List<MappingArea> mappings = new ArrayList<>();
        List<String> notExistList = new ArrayList<>();
        list.forEach(e -> {
            String[] split = e.split(",");
            String key = split[3] + "," + split[4] + "," + split[5];
            boolean isNew = false;
            MappingArea code = omsMap.get(key);

            if(code == null) {
                code = addressMapper.selectAreaCodeByName(split[3], split[4].equals("市辖区") ? split[3] : split[4], split[5]);
                isNew = true;
            }
            if (code != null) {

                if(isNew) {
                    omsMap.put(key, code);
                    code.setSystemProvinceName(split[3]);
                    code.setSystemCityName(split[4]);
                    code.setSystemAreaName(split[5]);
                    code.setSystemCode(1);
                    mappings.add(code);
                }

                MappingArea mapping = new MappingArea();
                mapping.setSystemProvinceName(split[0]);
                mapping.setSystemCityName(split[1]);
                mapping.setSystemAreaName(split[2]);
                mapping.setSystemCode(systemType);
                mapping.setStandardProvinceCode(code.getStandardProvinceCode());
                mapping.setStandardCityCode(code.getStandardCityCode());
                mapping.setStandardAreaCode(code.getStandardAreaCode());

                if(mappings.size() >= 200) {
                    addressMapper.insertMappingArea(mappings);
                    mappings.clear();
                }
                mappings.add(mapping);

            } else {
                notExistList.add(e);
            }
        });
        if(!CollectionUtils.isEmpty(mappings)) {
            addressMapper.insertMappingArea(mappings);
        }
        System.out.println(notExistList.size());
        FileUtils.writeLines(new File(notMappingFilepath), notExistList);
    }

    private void incrementUpdateCounty(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        Elements trs = document.getElementsByTag("tr");
        Iterator<Element> trIterator = trs.iterator();
        ModifyInfo modifyInfo = new ModifyInfo();
        List<IncrementCounty> modifyCountyList = new ArrayList<>();
        String height = null;
        String style = null;
        while(trIterator.hasNext()) {
            Element tr = trIterator.next();

            Elements td = tr.getElementsByTag("td");
            Iterator<Element> iterator = td.iterator();
            while (iterator.hasNext()) {
                Element next = iterator.next();
                if(next.text().equals("序号")) {
                    height = next.parent().attr("height");
                    style = next.parent().attr("style");
                }
            }
            if(!Objects.equals(tr.attr("height"), height) || !Objects.equals(style, tr.attr("style"))) {
                continue;
            }
            try {
                IncrementCounty county = IncrementCounty.getInstance(tr.getElementsByTag("td"), modifyInfo);
                if(county != null) {
                    county.setFetchUrl(url);
                    if (county.getCurrentCode() == null) {
                        StringBuilder builder = new StringBuilder(county.getModifyReason());
                        StringBuilder reverse = builder.reverse();
                        int indexCreate = reverse.indexOf("设");
                        if (indexCreate == 1 || indexCreate == 2) {
                            IncrementCounty last = modifyCountyList.get(modifyCountyList.size() - 1);
                            county.setCurrentCode(last.getCurrentCode());
                            county.setCurrentName(last.getCurrentName());
                        }
                    }
                    modifyCountyList.add(county);
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        addressMapper.insertIncrementCounty(modifyCountyList);

    }

    private void saveStandardAddress() throws IOException {
        Document document = Jsoup.connect("http://www.mca.gov.cn//article/sj/xzqh/2020/2020/2020092500801.html").get();
        Elements td = document.getElementsByTag("td");
        Iterator<Element> iterator = td.iterator();

        boolean cityFlag = false;
        //用来拼接城市的code和name
        String tempCityStr = "";
        //用来拼接区县的code和name
        String tempAreaStr = "";
        Map<String, List<String>> map = new TreeMap<>(Comparator.naturalOrder());
        while (iterator.hasNext()) {
            Element next = iterator.next();
            //处理市、省
            Elements cityElements = next.getElementsByAttributeValue("class", "xl7024734");
            Iterator<Element> cityIterator = cityElements.iterator();
            while (cityIterator.hasNext()) {
                Element cityElement = cityIterator.next();
                String city = cityElement.text();
                if (StringUtils.isBlank(city)) continue;
                if (!NumberUtils.isDigits(city)) {
                    tempCityStr = tempCityStr.concat("|").concat(city);
                } else {
                    //因为省和下面的市在一起连着，所以需要在这里特殊处理一下省的情况
                    if (!StringUtils.isBlank(tempCityStr) && !map.containsKey(tempCityStr)) {
                        map.put(tempCityStr, Collections.emptyList());
                    }
                    tempCityStr = city;
                }
                cityFlag = true;
            }

            //处理区县
            Elements areaElements = next.getElementsByAttributeValue("class", "xl7124734");
            Iterator<Element> areaIterator = areaElements.iterator();
            while (areaIterator.hasNext()) {
                Element areaElement = areaIterator.next();
                String area = areaElement.text();
                if (StringUtils.isBlank(area)) continue;
                if (cityFlag) {
                    tempAreaStr = "";
                    map.put(tempCityStr, new ArrayList<>());
                    cityFlag = false;
                }

                if (NumberUtils.isDigits(area)) {
                    tempAreaStr = tempCityStr.concat("-").concat(area);
                } else {
                    tempAreaStr = tempAreaStr.concat("|").concat(area);
                    map.get(tempCityStr).add(tempAreaStr.split("-")[1]);
                    tempAreaStr = "";
                }
            }
        }
        if(!map.containsKey(tempCityStr)) {
            map.put(tempCityStr, Collections.emptyList());
        }

        List<StandardProvince> provinces = new ArrayList<>();
        List<StandardCity> cities = new ArrayList<>();
        List<StandardArea> areas = new ArrayList<>();
        HashSet<String> areaCodeSet = new HashSet<>();
        HashSet<String> provinceCodeSet = new HashSet<>();
        HashSet<String> cityCodeSet = new HashSet<>();
        map.forEach((k, list) -> {
            String[] split = k.split("\\|");
            Integer code = Integer.valueOf(split[0]);
            if (code % 10000 == 0) {
                StandardProvince province = new StandardProvince();
                province.setProvinceCode(code.toString());
                province.setProvinceName(split[1]);
                provinces.add(province);
                provinceCodeSet.add(code.toString());
            }
            Integer provinceCode = code / 10000 * 10000;
            if (list.size() > 0) {
                StandardCity city = new StandardCity();
                city.setCityCode(code.toString());
                city.setCityName(split[1]);
                if (provinceCodeSet.contains(provinceCode.toString())) {
                    city.setProvinceCode(provinceCode.toString());
                }
                cities.add(city);
                cityCodeSet.add(code.toString());

                list.forEach(e -> {
                    StandardArea area = new StandardArea();
                    String[] strings = e.split("\\|");
                    area.setAreaCode(strings[0]);
                    area.setAreaName(strings[1]);
                    area.setCityCode(code.toString());
                    areas.add(area);
                    areaCodeSet.add(area.getAreaCode());
                });
            } else {
                if (!cityCodeSet.contains(code) && code % 10000 != 0) {
                    StandardCity city = new StandardCity();
                    city.setCityCode(code.toString());
                    city.setCityName(split[1]);
                    city.setProvinceCode(provinceCode.toString());
                    cities.add(city);
                    if(list.size() == 0) {
                        StandardArea area = new StandardArea();
                        area.setAreaCode(code.toString());
                        area.setAreaName(split[1]);
                        area.setCityCode(code.toString());
                        areas.add(area);
                        areaCodeSet.add(area.getAreaCode());
                    }
                }
            }
        });

        addressMapper.insertProvince(provinces);

        addressMapper.insertCity(cities);

        List<StandardArea> saveArea = new ArrayList<>(200);
        for (int i = 0; i < areas.size(); i++) {
            if (saveArea.size() >= 200) {
                addressMapper.insertArea(saveArea);
                saveArea.clear();
            }
            saveArea.add(areas.get(i));
        }
        if (!CollectionUtils.isEmpty(saveArea)) {
            addressMapper.insertArea(saveArea);
        }

        try {
            String countyStr = FileUtils.readFileToString(new File("D:/streets.json"));
            List<JSONObject> jsonArray = JSONArray.parseArray(countyStr, JSONObject.class);
            //{"areaCode":"110101","code":"110101001","cityCode":"1101","provinceCode":"11","name":"东华门街道"}
            int size = jsonArray.size();
            int partition = 200;
            int divideCount = size % partition == 0 ? size / partition : (size / partition + 1);

            IntStream.range(0, divideCount).forEach(e -> {
                List<StandardCounty> saveCountyList = jsonArray.stream().skip(e * partition).limit(partition).map(object -> {
                    StandardCounty county = new StandardCounty();
                    county.setCountyCode(object.getString("code"));
                    county.setCountyName(object.getString("name"));
                    String areaCode = object.getString("areaCode");
                    if (areaCodeSet.contains(areaCode)) {
                        county.setAreaCode(areaCode);
                    }
                    return county;
                }).collect(Collectors.toList());
                addressMapper.insertCounty(saveCountyList);
            });


            List<StandardCounty> countyList = jsonArray.stream().map(e -> {
                StandardCounty county = new StandardCounty();
                county.setCountyCode(e.getString("code"));
                county.setCountyName(e.getString("name"));
                String areaCode = e.getString("areaCode");
                if (areaCodeSet.contains(areaCode)) {
                    county.setAreaCode(areaCode);
                }
                return county;
            }).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void region() throws URISyntaxException {
        String list = client.getDistrictList(new URI("http://restapi.amap.com"), "2b222b483610149c5b6c99ccbe7a415e", "中国", "1", "base");

        JSONArray array = JSONObject.parseObject(list).getJSONArray("districts");

        TreeMap<Integer, String> provinceMap = new TreeMap<>(Comparator.naturalOrder());

        ((JSONObject) array.get(0)).getJSONArray("districts").forEach(
                e -> {
                    JSONObject e1 = (JSONObject) e;
                    provinceMap.put(e1.getInteger("adcode"), e1.getString("name"));
                }
        );
        provinceMap.entrySet().forEach(e -> System.out.println(e.getKey() + "-" + e.getValue()));
        System.out.println(provinceMap.size());

        TreeMap<Integer, String> cityMap = new TreeMap<>(Comparator.naturalOrder());
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

        TreeMap<Integer, String> areaMap = new TreeMap<>(Comparator.naturalOrder());
        cityMap.keySet().stream().filter(e -> e < 810001).forEach(key -> {
            try {
                String cityName = cityMap.get(key);
                String areas = client.getDistrictList(new URI("http://restapi.amap.com"), "2b222b483610149c5b6c99ccbe7a415e", cityName, "1", "base");
                JSONArray areaArray = JSONObject.parseObject(areas).getJSONArray("districts");


                ((JSONObject) areaArray.get(0)).getJSONArray("districts").forEach(
                        element -> {
                            JSONObject e1 = (JSONObject) element;
                            areaMap.put(e1.getInteger("adcode"), e1.getString("name"));
                        }
                );
            } catch (URISyntaxException uriSyntaxException) {
                uriSyntaxException.printStackTrace();
            }

        });

        areaMap.entrySet().forEach(e -> System.out.println(e.getValue() + "-" + e.getKey()));
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

                        if (vo.getStatus() != 0) {
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
