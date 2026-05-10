package com.supremepole.vertx.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class PublisherVerticle extends AbstractVerticle {
    
    @Override
    public void start(Promise<Void> startPromise) {
        System.out.println("PublisherVerticle 启动");
        
        // 定时发布消息（发布-订阅模式）
        vertx.setPeriodic(2000, id -> {
            vertx.eventBus().publish("news.topic", "新闻消息: " + System.currentTimeMillis());
            System.out.println("发布了一条新闻消息");
        });
        
        startPromise.complete();
    }
}