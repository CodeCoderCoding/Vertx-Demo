package com.supremepole.vertx.web.handler;

import io.vertx.ext.web.RoutingContext;

public class HelloHandler {
    public void handle(RoutingContext context) {
        context.response()
                .putHeader("content-type", "text/plain")
                .end("Hello from Vertx Web!");
    }
}