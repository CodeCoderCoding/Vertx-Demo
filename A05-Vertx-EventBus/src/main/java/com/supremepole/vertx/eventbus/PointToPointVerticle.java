package com.supremepole.vertx.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;

public class PointToPointVerticle extends AbstractVerticle {
    
    @Override
    public void start(Promise<Void> startPromise) {
        System.out.println("PointToPointVerticle 启动");
        
        // 发送点对点消息（无回复）
        vertx.setTimer(1000, id -> {
            // 发送消息（不需要回复）
            vertx.eventBus().send("direct.message", "这是一条点对点消息");
            System.out.println("发送了点对点消息");
        });
        
        // 接收点对点消息
        vertx.eventBus().consumer("direct.message", message -> {
            System.out.println("收到点对点消息: " + message.body());
        });
        
        // 发送带 headers 的消息
        vertx.setTimer(2000, id -> {
            DeliveryOptions options = new DeliveryOptions()
                .addHeader("content-type", "application/json")
                .addHeader("priority", "high");
            vertx.eventBus().send("direct.message", "带 headers 的消息", options);
        });
        
        startPromise.complete();
    }
}