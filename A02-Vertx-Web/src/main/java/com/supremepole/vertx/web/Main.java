package com.supremepole.vertx.web;

import io.vertx.core.Vertx;
import com.supremepole.vertx.web.config.RouterConfig;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        RouterConfig.setupRouter(vertx);
    }
}