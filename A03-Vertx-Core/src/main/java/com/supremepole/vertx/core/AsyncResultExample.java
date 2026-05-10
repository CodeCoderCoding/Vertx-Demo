package com.supremepole.vertx.core;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class AsyncResultExample {
    
    public static void runExample(Vertx vertx) {
        System.out.println("\n=== AsyncResult 示例 ===");
        
        // 1. 模拟异步操作返回 AsyncResult
        performAsyncOperation(vertx, true, ar -> {
            handleAsyncResult(ar);
        });
        
        performAsyncOperation(vertx, false, ar -> {
            handleAsyncResult(ar);
        });
        
        // 2. 使用 lambda 简化 Handler
        performAsyncOperation(vertx, true, ar -> {
            if (ar.succeeded()) {
                System.out.println("Lambda 处理成功: " + ar.result());
            }
        });
    }
    
    private static void performAsyncOperation(Vertx vertx, boolean success, Handler<AsyncResult<String>> handler) {
        vertx.setTimer(500, id -> {
            if (success) {
                handler.handle(io.vertx.core.Future.succeededFuture("操作成功"));
            } else {
                handler.handle(io.vertx.core.Future.failedFuture(new RuntimeException("操作失败")));
            }
        });
    }
    
    private static void handleAsyncResult(AsyncResult<String> result) {
        if (result.succeeded()) {
            System.out.println("操作成功: " + result.result());
        } else {
            System.out.println("操作失败: " + result.cause().getMessage());
        }
    }
}