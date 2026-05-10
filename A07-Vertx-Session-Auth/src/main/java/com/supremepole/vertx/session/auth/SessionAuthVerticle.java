package com.supremepole.vertx.session.auth;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

import java.util.HashMap;
import java.util.Map;

public class SessionAuthVerticle extends AbstractVerticle {
    
    // 模拟用户数据库
    private Map<String, String> users = new HashMap<>();
    private JWTAuth jwtAuth;
    
    @Override
    public void start(Promise<Void> startPromise) {
        // 初始化用户数据
        users.put("admin", "admin123");
        users.put("user", "user123");
        
        // 配置 JWT
        jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
            .addPubSecKey(new PubSecKeyOptions()
                .setAlgorithm("HS256")
                .setBuffer("supersecretkeytoken")));
        
        Router router = Router.router(vertx);
        
        // Cookie 处理
        router.route().handler(CookieHandler.create());
        
        // Session 处理
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        
        // 请求体处理
        router.route().handler(BodyHandler.create());
        
        // 公开路由
        router.post("/login").handler(this::handleLogin);
        router.post("/login-jwt").handler(this::handleLoginJwt);
        router.get("/public").handler(this::handlePublic);
        
        // Session 保护的路由
        router.get("/session/protected").handler(this::requireSession).handler(this::handleSessionProtected);
        
        // JWT 保护的路由
        router.get("/jwt/protected").handler(JWTAuthHandler.create(jwtAuth)).handler(this::handleJwtProtected);
        
        // Cookie 示例
        router.get("/set-cookie").handler(this::handleSetCookie);
        router.get("/get-cookie").handler(this::handleGetCookie);
        
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080, ar -> {
                if (ar.succeeded()) {
                    System.out.println("Session Auth Server started on port 8080");
                    startPromise.complete();
                } else {
                    startPromise.fail(ar.cause());
                }
            });
    }
    
    private void handleLogin(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String username = body.getString("username");
        String password = body.getString("password");
        
        if (users.containsKey(username) && users.get(username).equals(password)) {
            // 设置 Session
            ctx.session().put("username", username);
            ctx.response()
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("success", true).put("message", "登录成功").toString());
        } else {
            ctx.response()
                .setStatusCode(401)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("success", false).put("message", "用户名或密码错误").toString());
        }
    }
    
    private void handleLoginJwt(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String username = body.getString("username");
        String password = body.getString("password");
        
        if (users.containsKey(username) && users.get(username).equals(password)) {
            // 生成 JWT token
            String token = jwtAuth.generateToken(
                new JsonObject().put("username", username),
                new JWTOptions().setExpiresIn("30m")
            );
            ctx.response()
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("success", true).put("token", token).toString());
        } else {
            ctx.response()
                .setStatusCode(401)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("success", false).put("message", "用户名或密码错误").toString());
        }
    }
    
    private void requireSession(RoutingContext ctx) {
        if (ctx.session().get("username") == null) {
            ctx.response()
                .setStatusCode(401)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("success", false).put("message", "未登录").toString());
        } else {
            ctx.next();
        }
    }
    
    private void handleSessionProtected(RoutingContext ctx) {
        String username = ctx.session().get("username");
        ctx.response()
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("message", "欢迎 " + username + "，这是受保护的资源").toString());
    }
    
    private void handleJwtProtected(RoutingContext ctx) {
        String username = ctx.user().principal().getString("username");
        ctx.response()
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("message", "欢迎 " + username + "，这是 JWT 保护的资源").toString());
    }
    
    private void handlePublic(RoutingContext ctx) {
        ctx.response()
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("message", "这是公开资源").toString());
    }
    
    private void handleSetCookie(RoutingContext ctx) {
        ctx.addCookie(io.vertx.ext.web.Cookie.cookie("mycookie", "myvalue"))
           .addCookie(io.vertx.ext.web.Cookie.cookie("session", "abc123").setHttpOnly(true));
        ctx.response()
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("message", "Cookie 设置成功").toString());
    }
    
    private void handleGetCookie(RoutingContext ctx) {
        String mycookie = ctx.getCookie("mycookie") != null ? ctx.getCookie("mycookie").getValue() : "未设置";
        ctx.response()
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("mycookie", mycookie).toString());
    }
}