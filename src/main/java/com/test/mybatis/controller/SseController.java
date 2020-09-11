package com.test.mybatis.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@RestController
public class SseController {
    Map<String,SseEmitter> cache = new HashMap<>();
    Map<String, List<Object>> data = new HashMap<>();
    List<Object> now = new ArrayList<>();

    @GetMapping(value = "/sse/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter create(@PathVariable String id) {
        SseEmitter emitter = new SseEmitter();
        cache.put(id, emitter);
        emitter.onTimeout(() -> data.put(id, now));
        return emitter;
    }

    @GetMapping(value = "/sse/send/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void send(@PathVariable String id) {
        doSend(cache.get(id));
    }

    public void doSend(SseEmitter emitter) {
        int i = 0;
        while(true) {
            try {
                emitter.send("id:" + i);
                TimeUnit.SECONDS.sleep(1);
                ++i;
                now.add(i);
            } catch (Exception ioException) {
                ioException.printStackTrace();
            }
        }
    }

    @GetMapping(value = "/sse/redo/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void rebuild(@PathVariable String id) {
        SseEmitter emitter = cache.get(id);
        List<Object> list = data.get(id);
        list.forEach(e -> {
            try {
                emitter.send(e);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        emitter.complete();
    }

}
