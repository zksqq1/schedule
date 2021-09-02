package com.test.mybatis.controller;

import com.test.mybatis.client.ExternalApiClient;
import com.test.mybatis.model.AddressDetail;
import com.test.mybatis.model.GdParseLocation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 */
@Api(tags = "地址解析")
@RestController
public class AddressController {

    @Autowired
    private ExternalApiClient client;


    @ApiOperation("根据地址获取经纬度、标准化的省市区等信息")
    @PostMapping("/location/list")
    public List<AddressDetail> getAddressDetailListByAddress(
            @ApiParam(name = "addressList", value = "详细地址列表", required = true) @RequestBody List<String> addressList) {
        if(addressList.stream().filter(StringUtils::hasText).count() == 0) {
            return Collections.emptyList();
        }
        return doGetAddressDetailListByAddress(addressList);
    }

    private List<AddressDetail> doGetAddressDetailListByAddress(List<String> addressList) {
        int splitSize = 10;
        int end = addressList.size() % splitSize == 0 ? addressList.size() / splitSize : addressList.size() / splitSize + 1;
        List<CompletableFuture<List<AddressDetail>>> futureList = IntStream.range(0, end)
                .mapToObj(e -> addressList.stream().skip(e * splitSize).limit(splitSize).collect(Collectors.toList()))
                .map(e -> CompletableFuture.supplyAsync(getLocationFromApi(e)))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        List<AddressDetail> list = futureList.stream().map(element -> {
            try {
                return element.get();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return Collections.EMPTY_LIST;
        }).flatMap(Collection<AddressDetail>::stream).collect(Collectors.toList());

        return list;
    }

    private Supplier<List<AddressDetail>> getLocationFromApi(List<String> addressList) {
        return () -> {
            StringJoiner joiner = new StringJoiner("|");
            addressList.stream().forEach(joiner::add);
            String address = joiner.toString();
            List<AddressDetail> list = Collections.emptyList();
            try {
                GdParseLocation gdLocation = client.getGdLngLat(new URI("https://restapi.amap.com/"), "2b222b483610149c5b6c99ccbe7a415e", address, "json", null, "true");
                if(Objects.equals(gdLocation.getCount(), String.valueOf(addressList.size()))) {
                    list = IntStream.range(0, gdLocation.getGeoCodes().size())
                            .mapToObj(index -> {
                                AddressDetail detail = AddressDetail.transferFromGd(gdLocation.getGeoCodes().get(index));
                                detail.setRequestAddress(addressList.get(index));
                                return detail;
                            }).collect(Collectors.toList());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        };
    }
}
