package com.supremepole.vertx.eventbus;

import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        
        // 部署各个 Verticle
        vertx.deployVerticle(new SenderVerticle());
        vertx.deployVerticle(new ReceiverVerticle());
        vertx.deployVerticle(new PublisherVerticle());
        vertx.deployVerticle(new SubscriberVerticle());
        vertx.deployVerticle(new PointToPointVerticle());
    }
}