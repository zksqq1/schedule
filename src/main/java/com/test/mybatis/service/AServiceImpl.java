package com.test.mybatis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.mybatis.entity.User;
import com.test.mybatis.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author lin.
 */
@Service
public class AServiceImpl implements AService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void findTest() {
        User user = userMapper.selectByUserId("123");
        try {
            System.out.println(new ObjectMapper().writeValueAsString(user));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    @Override
    @Transactional
    public void tranTest() {
        User user = new User();
        user.setUserId("aaaa1");
        user.setUserCode("bbbb1");
        user.setPhone("aaaaa2");
        userMapper.insertUser(user);
        System.out.println(1 / 0);
    }

    @Override
    public void tranTest2() {
        User user = new User();
        user.setUserId("aaaa");
        user.setUserCode("bbbb");
        user.setPhone("aaaaa");
        userMapper.insertUser(user);
    }


}
