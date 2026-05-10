package com.supremepole.vertx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

public class ConfigurableVerticle extends AbstractVerticle {
    
    @Override
    public void start(Promise<Void> startPromise) {
        // 读取配置
        JsonObject config = config();
        String message = config.getString("message", "默认消息");
        int timeout = config.getInteger("timeout", 3000);
        
        System.out.println("ConfigurableVerticle 启动");
        System.out.println("配置消息: " + message);
        System.out.println("配置超时: " + timeout + "ms");
        
        // 使用配置
        vertx.setTimer(timeout, id -> {
            System.out.println("定时器触发，配置超时时间已到");
        });
        
        startPromise.complete();
    }
    
    @Override
    public void stop(Promise<Void> stopPromise) {
        System.out.println("ConfigurableVerticle 停止");
        stopPromise.complete();
    }
}