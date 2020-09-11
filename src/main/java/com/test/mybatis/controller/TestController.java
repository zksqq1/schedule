package com.test.mybatis.controller;

import com.test.mybatis.entity.User;
import com.test.mybatis.mapper.UserMapper;
import com.test.mybatis.service.AService;
import com.test.mybatis.task.MyScheduleTaskBuilder;
import com.test.mybatis.task.ScheduleTaskHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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
    private ScheduleTaskHolder fetch;

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

    @GetMapping(value = {"/test", "/asd"})
    public String test(@RequestBody String asd) {
        aService.findTest();
        return asd;
    }

    @GetMapping("/cron")
    public void update(@RequestParam("cron") String cron) {
        fetch.update("test", cron);
    }

    @GetMapping("/cron/add")
    public void add() {
        fetch.add(new MyScheduleTaskBuilder("test1", "0/1 * * * * ?", () -> System.out.println(System.currentTimeMillis() + "test1")));

    }

}
