package com.supremepole.vertx.core;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

public class PromiseFutureExample {
    
    public static void runExample(Vertx vertx) {
        System.out.println("\n=== Promise & Future 示例 ===");
        
        // 1. 创建 Promise 并完成
        Promise<String> promise = Promise.promise();
        Future<String> future = promise.future();
        
        future.onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("Future 成功: " + ar.result());
            } else {
                System.out.println("Future 失败: " + ar.cause().getMessage());
            }
        });
        
        // 完成 Promise
        promise.complete("Hello Promise!");
        
        // 2. 失败的 Promise
        Promise<Integer> failedPromise = Promise.promise();
        failedPromise.future().onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("这个不会执行");
            } else {
                System.out.println("Future 失败示例: " + ar.cause().getMessage());
            }
        });
        failedPromise.fail(new RuntimeException("故意失败"));
        
        // 3. 使用 Vertx 异步操作返回 Future
        vertx.fileSystem().exists("test.txt").onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("文件存在: " + ar.result());
            }
        });
        
        // 4. 组合多个 Future
        Future<String> future1 = Future.succeededFuture("Hello");
        Future<String> future2 = Future.succeededFuture("World");
        
        Future.all(future1, future2).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("所有 Future 都成功");
            }
        });
    }
}