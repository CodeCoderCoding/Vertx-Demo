package com.supremepole.vertx.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class SubscriberVerticle extends AbstractVerticle {
    
    @Override
    public void start(Promise<Void> startPromise) {
        System.out.println("SubscriberVerticle 启动");
        
        // 订阅主题
        vertx.eventBus().consumer("news.topic", message -> {
            System.out.println("订阅者收到新闻: " + message.body());
        });
        
        // 多个订阅者可以订阅同一个主题
        vertx.eventBus().consumer("news.topic", message -> {
            System.out.println("另一个订阅者收到新闻: " + message.body());
        });
        
        startPromise.complete();
    }
}