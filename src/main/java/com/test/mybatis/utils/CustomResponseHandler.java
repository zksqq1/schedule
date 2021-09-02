package com.test.mybatis.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 *  自定义响应处理类
 */
public class CustomResponseHandler<T> implements ResponseHandler<T> {
    private Class<T> clazz;

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T handleResponse(HttpResponse response) throws IOException {
        String responseStr = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            return new ObjectMapper().readValue(responseStr, clazz);
        }
        return null;
    }

    public static void test() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest servletRequest = attributes.getRequest();
        System.out.println(servletRequest);
    }
}
