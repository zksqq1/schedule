package com.test.mybatis.controller;

import com.alibaba.fastjson.JSON;
import com.test.mybatis.config.MyProperties;
import com.test.mybatis.entity.StandardProvince;
import com.test.mybatis.entity.User;
import com.test.mybatis.mapper.AMapper;
import com.test.mybatis.mapper.BMapper;
import com.test.mybatis.mapper.UserMapper;
import com.test.mybatis.service.AService;
import com.test.mybatis.utils.TestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 *
 */
@RestController
public class TestController {
    @Autowired
    private AService aService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MyProperties properties;


    @GetMapping("/")
    public User test() {
        return userMapper.selectByUserId("123");
    }

    @GetMapping("/set")
    public String set() {

        ServletRequestAttributes attributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = attributes.getRequest();
        request.setAttribute("name", "test");
        System.out.println(request);
        return "OK";
    }
    @GetMapping("/get")
    public String get() {
        ServletRequestAttributes attributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = attributes.getRequest();
        return Optional.ofNullable(request.getAttribute("name")).orElse("error").toString();
    }

    @GetMapping("/testA")
    public String testA() {
        return JSON.toJSONString(properties);
    }

    @GetMapping("/testPath/{param}")
    public String testPath(@PathVariable String param) {
        return param;
    }

    @GetMapping("testOk")
    public String testOk() {
        System.out.println(aService);
        aService.findTest();
        return "OK";
    }

    @GetMapping("/transTest")
    public String testTrans() {
        aService.tranTest();
        return "OK";
    }

    @Autowired
    private AMapper aMapper;
    @Autowired
    private BMapper bMapper;

    @GetMapping("/testMapper")
    public StandardProvince test(@RequestParam("id") Integer id) {
        System.out.println(aMapper);
        System.out.println(bMapper);
        return id != null && id == 1 ? find(aMapper) : find(bMapper);
    }

    private StandardProvince find(TestMapper mapper) {
        return mapper.test();
    }
}
