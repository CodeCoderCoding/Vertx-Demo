package com.supremepole.vertx.web.advanced;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.Arrays;

public class WebAdvancedVerticle extends AbstractVerticle {
    
    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);
        
        // CORS 处理
        router.route().handler(CorsHandler.create("*")
            .allowedMethods(Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE))
            .allowedHeaders(Arrays.asList("Content-Type", "Authorization")));
        
        // 请求体处理
        router.route().handler(BodyHandler.create()
            .setUploadsDirectory("uploads")
            .setDeleteUploadedFilesOnEnd(true));
        
        // 路由参数示例
        router.get("/users/:id").handler(this::handleUserById);
        router.get("/users/:id/posts/:postId").handler(this::handleUserPost);
        
        // 查询参数示例
        router.get("/search").handler(this::handleSearch);
        
        // JSON 请求体示例
        router.post("/users").handler(this::handleCreateUser);
        
        // 文件上传示例
        router.post("/upload").handler(this::handleFileUpload);
        
        // 响应类型示例
        router.get("/json").handler(this::handleJsonResponse);
        router.get("/html").handler(this::handleHtmlResponse);
        
        // 重定向示例
        router.get("/redirect").handler(this::handleRedirect);
        
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080, ar -> {
                if (ar.succeeded()) {
                    System.out.println("Web Advanced Server started on port 8080");
                    startPromise.complete();
                } else {
                    startPromise.fail(ar.cause());
                }
            });
    }
    
    private void handleUserById(RoutingContext ctx) {
        String userId = ctx.pathParam("id");
        ctx.response()
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("userId", userId).put("name", "User " + userId).toString());
    }
    
    private void handleUserPost(RoutingContext ctx) {
        String userId = ctx.pathParam("id");
        String postId = ctx.pathParam("postId");
        ctx.response()
            .putHeader("content-type", "application/json")
            .end(new JsonObject()
                .put("userId", userId)
                .put("postId", postId)
                .put("title", "Post " + postId)
                .toString());
    }
    
    private void handleSearch(RoutingContext ctx) {
        String query = ctx.queryParam("q").get(0);
        int limit = Integer.parseInt(ctx.queryParam("limit").getOrDefault(0, "10"));
        ctx.response()
            .putHeader("content-type", "application/json")
            .end(new JsonObject()
                .put("query", query)
                .put("limit", limit)
                .put("results", Arrays.asList("result1", "result2"))
                .toString());
    }
    
    private void handleCreateUser(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String name = body.getString("name");
        String email = body.getString("email");
        
        JsonObject response = new JsonObject()
            .put("id", "123")
            .put("name", name)
            .put("email", email)
            .put("createdAt", System.currentTimeMillis());
        
        ctx.response()
            .setStatusCode(201)
            .putHeader("content-type", "application/json")
            .end(response.toString());
    }
    
    private void handleFileUpload(RoutingContext ctx) {
        ctx.fileUploads().forEach(upload -> {
            System.out.println("文件名: " + upload.fileName());
            System.out.println("文件大小: " + upload.size());
            System.out.println("上传路径: " + upload.uploadedFileName());
        });
        
        ctx.response()
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("message", "文件上传成功").toString());
    }
    
    private void handleJsonResponse(RoutingContext ctx) {
        JsonObject data = new JsonObject()
            .put("message", "Hello JSON")
            .put("success", true)
            .put("count", 42);
        ctx.response()
            .putHeader("content-type", "application/json")
            .end(data.toString());
    }
    
    private void handleHtmlResponse(RoutingContext ctx) {
        String html = "<html><body><h1>Hello HTML</h1><p>这是一个 HTML 响应</p></body></html>";
        ctx.response()
            .putHeader("content-type", "text/html")
            .end(html);
    }
    
    private void handleRedirect(RoutingContext ctx) {
        ctx.response().setStatusCode(302).putHeader("Location", "/json").end();
    }
}