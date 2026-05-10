package com.supremepole.vertx.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class SenderVerticle extends AbstractVerticle {
    
    @Override
    public void start(Promise<Void> startPromise) {
        System.out.println("SenderVerticle 启动");
        
        // 发送消息并等待回复
        vertx.eventBus().request("echo.address", "Hello Event Bus!", reply -> {
            if (reply.succeeded()) {
                System.out.println("收到回复: " + reply.result().body());
            } else {
                System.out.println("发送失败: " + reply.cause().getMessage());
            }
        });
        
        // 发送带超时的消息
        vertx.eventBus().request("echo.address", "带超时的消息", 5000, reply -> {
            if (reply.succeeded()) {
                System.out.println("超时消息收到回复: " + reply.result().body());
            }
        });
        
        startPromise.complete();
    }
}