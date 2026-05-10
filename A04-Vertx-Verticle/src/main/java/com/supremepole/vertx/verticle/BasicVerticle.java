package com.supremepole.vertx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class BasicVerticle extends AbstractVerticle {
    
    @Override
    public void start(Promise<Void> startPromise) {
        System.out.println("BasicVerticle 启动");
        
        // 启动 HTTP 服务器
        vertx.createHttpServer()
            .requestHandler(request -> {
                request.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello from BasicVerticle!");
            })
            .listen(8080, ar -> {
                if (ar.succeeded()) {
                    System.out.println("BasicVerticle HTTP 服务器启动在端口 8080");
                    startPromise.complete();
                } else {
                    startPromise.fail(ar.cause());
                }
            });
    }
    
    @Override
    public void stop(Promise<Void> stopPromise) {
        System.out.println("BasicVerticle 停止");
        stopPromise.complete();
    }
}