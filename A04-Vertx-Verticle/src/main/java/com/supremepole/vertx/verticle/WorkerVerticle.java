package com.supremepole.vertx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class WorkerVerticle extends AbstractVerticle {
    
    private int instanceId;
    
    @Override
    public void start(Promise<Void> startPromise) {
        // 获取部署配置中的实例 ID
        instanceId = config().getInteger("instanceId", 0);
        System.out.println("WorkerVerticle 启动，实例 ID: " + instanceId);
        
        // 注册 Event Bus 处理器
        vertx.eventBus().consumer("worker.task", message -> {
            String task = message.body().toString();
            System.out.println("WorkerVerticle-" + instanceId + " 处理任务: " + task);
            
            // 模拟耗时操作
            vertx.setTimer(1000, id -> {
                message.reply("WorkerVerticle-" + instanceId + " 完成任务: " + task);
            });
        });
        
        startPromise.complete();
    }
    
    @Override
    public void stop(Promise<Void> stopPromise) {
        System.out.println("WorkerVerticle-" + instanceId + " 停止");
        stopPromise.complete();
    }
}