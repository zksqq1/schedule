package com.test.mybatis.controller;

import com.test.mybatis.entity.User;
import com.test.mybatis.mapper.UserMapper;
import com.test.mybatis.service.AService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.DispatcherServlet;

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

    @GetMapping(value = {"/test", "/asd"})
    public String test(@RequestBody String asd) {
        aService.findTest();
        return asd;
    }

    @GetMapping("/page")
    @Transactional
    public void test1() {
//        PageHelper.startPage(10, 2);
//
//        Page<User> users = userMapper.selectPage();
//
//        users.stream().forEach(e -> System.out.println(e.getUserId()));

//        throw new ClientForbiddenException("测试异常", "1");

    }
}
