package com.test.mybatis.controller;

import com.test.mybatis.entity.User;
import com.test.mybatis.exception.CustomException;
import com.test.mybatis.mapper.UserMapper;
import com.test.mybatis.service.AService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/**
 *
 */
@RestController
public class TestController {
    @Autowired
    private AService aService;

    @Autowired
    private UserMapper userMapper;

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
        throw new CustomException("error");
    }

    @GetMapping("/testPath/{param}")
    public String testPath(@PathVariable String param) {
        return param;
    }

    @GetMapping("testOk")
    public String testOk() {
        return "OK";
    }
}
