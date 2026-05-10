package com.supremepole.vertx.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class ReceiverVerticle extends AbstractVerticle {
    
    @Override
    public void start(Promise<Void> startPromise) {
        System.out.println("ReceiverVerticle 启动");
        
        // 注册消息处理器（请求-响应模式）
        vertx.eventBus().consumer("echo.address", message -> {
            System.out.println("收到消息: " + message.body());
            // 回复消息
            message.reply("Echo: " + message.body());
        });
        
        // 注册失败回复示例
        vertx.eventBus().consumer("fail.address", message -> {
            // 发送失败回复
            message.fail(500, "故意失败");
        });
        
        startPromise.complete();
    }
}