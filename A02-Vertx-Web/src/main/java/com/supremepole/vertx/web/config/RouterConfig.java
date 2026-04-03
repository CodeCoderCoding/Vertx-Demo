package com.supremepole.vertx.web.config;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import com.supremepole.vertx.web.handler.HelloHandler;
import com.supremepole.vertx.web.handler.UserHandler;

public class RouterConfig {
    public static void setupRouter(Vertx vertx) {
        Router router = Router.router(vertx);

        // register some handlers
        HelloHandler helloHandler = new HelloHandler();
        UserHandler userHandler = new UserHandler();

        // configure routes
        router.get("/hello").handler(helloHandler::handle);
        router.get("/user").handler(userHandler::getAllUsers);
        router.get("/user/:id").handler(userHandler::getUserById);
        router.post("/user").handler(userHandler::createUser);

        // start server with router
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8081).onComplete(result -> {
                    if (result.succeeded()) {
                        System.out.println("Server started on port 8081");
                    } else {
                        System.out.println("Failed to start server: " + result.cause());
                    }
                });
    }
}