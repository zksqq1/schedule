package com.test.mybatis;

import com.test.mybatis.controller.TestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

@SpringBootTest
class MybatisApplicationTests {
    @Autowired
    private TestController controller;

    @Test
    void contextLoads() {
        controller.test("123");
    }

}
