package com.supremepole.vertx.verticle;

import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        
        // 1. 部署单个 Verticle
        vertx.deployVerticle(new BasicVerticle(), ar -> {
            if (ar.succeeded()) {
                System.out.println("BasicVerticle 部署成功，部署 ID: " + ar.result());
            } else {
                System.out.println("BasicVerticle 部署失败: " + ar.cause().getMessage());
            }
        });
        
        // 2. 部署多个相同类型的 Verticle（水平扩展）
        vertx.deployVerticle(WorkerVerticle.class.getName(), deploymentOptions -> {
            deploymentOptions.setInstances(3);
        }, ar -> {
            if (ar.succeeded()) {
                System.out.println("WorkerVerticle 部署成功（3 个实例）");
            }
        });
        
        // 3. 部署带有配置的 Verticle
        io.vertx.core.DeploymentOptions options = new io.vertx.core.DeploymentOptions()
            .setConfig(new io.vertx.core.json.JsonObject()
                .put("message", "来自配置的消息")
                .put("timeout", 5000));
        vertx.deployVerticle(new ConfigurableVerticle(), options, ar -> {
            if (ar.succeeded()) {
                System.out.println("ConfigurableVerticle 部署成功");
            }
        });
    }
}