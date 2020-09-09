package com.test.mybatis.entity;


import lombok.Data;

/**
 * 用户信息(User)实体类
 *
 * @author lin
 * @since 2020-08-14 17:07:30
 */
@Data
public class User {
    /**
    * 用户id
    */
    private String userId;
    /**
    * 根据用户类型决定是studentId还是teacherId
    */
    private String userCode;
    /**
    * 用户姓名
    */
    private String username;
    /**
    * 用户类型：1学生。2家长，3机构用户
    */
    private String userType;
    /**
    * 密码
    */
    private String password;
    /**
    * 手机号
    */
    private String phone;
    /**
    * 如果是学生需要设置父母密码
    */
    private String parentToken;
    /**
    * 出生年份
    */
    private Integer birthYear;
    /**
    * 性别，1=男，0=女
    */
    private Integer sex;
    /**
    * 身份证号/学籍号()
    */
    private String identityId;
    /**
    * 头像地址
    */
    private String headImg;
    /**
    * 状态：0不可用，1可用
    */
    private String status;


}