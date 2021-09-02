package com.test.mybatis.mapper;

import com.github.pagehelper.Page;
import com.test.mybatis.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author lin.
 */
@Repository
public interface UserMapper {
    @Select("select * from user where user_id  = #{userId}")
    User selectByUserId(String userId);

    @Select("select * from user")
    Page<User> selectPage();

    @Insert("insert into user(user_id,user_code,phone) values(#{userId}, #{userCode},#{phone})")
    void insertUser(User user);
}
