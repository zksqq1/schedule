package com.test.mybatis.client.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import feign.Logger;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignLoggerFactory;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义feign的编解码(json数据转换)器
 */
public class CustomFeignConfig {
    @Bean
    public Encoder feignEncoder() {
        return new SpringEncoder(feignHttpMessageConverter());
    }

    @Bean
    public Decoder feignDecoder() {
        return new SpringDecoder(feignHttpMessageConverter());
    }

    @Bean
    Logger.Level feignLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    FeignLoggerFactory infoFeignLoggerFactory() {
        return new InfoFeignLoggerFactory();
    }

    /**
     * 设置解码器为fastjson
     *
     * @return
     */
    private ObjectFactory<HttpMessageConverters> feignHttpMessageConverter() {
        final HttpMessageConverters httpMessageConverters = new HttpMessageConverters(this.getFastJsonConverter());
        return () -> httpMessageConverters;
    }

    private FastJsonHttpMessageConverter getFastJsonConverter() {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(
                // 是否输出值为null的字段,默认为false
                SerializerFeature.WriteMapNullValue,
                // 禁用循环引用
                SerializerFeature.DisableCircularReferenceDetect);
        fastJsonConfig.setCharset(StandardCharsets.UTF_8);

        List<MediaType> fastMediaTypes = new ArrayList<>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON);

        converter.setSupportedMediaTypes(fastMediaTypes);
        converter.setFastJsonConfig(fastJsonConfig);
        return converter;
    }

    public static class InfoFeignLoggerFactory implements FeignLoggerFactory {

        @Override
        public Logger create(Class<?> type) {
            return new InfoFeignLogger(LoggerFactory.getLogger(type));
        }
    }

    public static class InfoFeignLogger extends Logger {
        private static ThreadLocal<Boolean> local = new ThreadLocal<>();

        private final org.slf4j.Logger logger;

        public InfoFeignLogger(org.slf4j.Logger logger) {
            this.logger = logger;
        }

        @Override
        protected void log(String configKey, String format, Object... args) {
            if (!logger.isInfoEnabled()) {
                return;
            }
            for (int i = 0; i < args.length; i++) {
                if(args[i] instanceof String) {
                    Boolean isResponse = local.get();
                    String arg = (String) args[i];
                    if(arg.startsWith("http://") || arg.startsWith("https://")) {
                        logger.info(String.format(methodTag(configKey) + " >>> 请求地址及方法 >>> " + format, args));
                        //GET方法没有请求体
                        if("GET".equalsIgnoreCase(args[0].toString())) {
                            local.set(Boolean.FALSE);
                        }
                        break;
                    } else if(arg.startsWith("{") && !"GET".equalsIgnoreCase(args[0].toString())) {
                        if(isResponse == null) {
                            isResponse = Boolean.FALSE;
                            local.set(Boolean.FALSE);
                        } else {
                            isResponse = Boolean.TRUE;
                        }
                        String str = isResponse ? ">>> 响应信息 >>> " : ">>> 请求信息 >>> ";
                        if(args.length > 1) {
                            logger.info(String.format(methodTag(configKey) + str + format, JSON.toJSONString(args)));
                        } else {
                            logger.info(String.format(methodTag(configKey) + str + format, JSON.toJSONString(arg)));
                        }
                        if(isResponse) {
                            local.remove();
                        }
                        break;
                    }
                }
            }
        }
    }
}
