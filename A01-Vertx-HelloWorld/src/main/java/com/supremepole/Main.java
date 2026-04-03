package com.supremepole;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;

public class Main {
    public static void main(String[] args) {
        // Create Vertx instance
        Vertx vertx = Vertx.vertx();

        // Create HTTP server
        HttpServer server = vertx.createHttpServer();

        // Handle request
        server.requestHandler(request -> {
            // Send response
            request.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello Vertx 5!");
        });

        // Listen on port
        server.listen(8080).onComplete(result -> {
            if (result.succeeded()) {
                System.out.println("Server started successfully, listening on port 8080");
            } else {
                System.out.println("Server failed to start: " + result.cause());
            }
        });
    }
}