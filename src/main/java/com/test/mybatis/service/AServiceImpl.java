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
    @Transactional
    public void findTest() {
        User user = userMapper.selectByUserId("123");
        try {
            System.out.println(new ObjectMapper().writeValueAsString(user));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }


}
