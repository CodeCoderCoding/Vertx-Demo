package com.supremepole.vertx.core;

import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        
        // 演示 Handler 基本用法
        HandlerExample.runExample(vertx);
        
        // 演示 Handler 模式与方法引用
        HandlerPatterns.runExample(vertx);
        
        // 演示 Promise 和 Future 基本用法
        PromiseFutureExample.runExample(vertx);
        
        // 演示 Future 组合操作（含业务场景）
        FutureCompositionExample.runExample(vertx);
        
        // 演示 AsyncResult
        AsyncResultExample.runExample(vertx);
        
        // 演示 Multi-Future 操作（含 CompositeFuture）
        MultiFutureExample.runExample(vertx);
        
        // 演示异步模式（超时、重试、降级）
        AsyncPatterns.runExample(vertx);
    }
}