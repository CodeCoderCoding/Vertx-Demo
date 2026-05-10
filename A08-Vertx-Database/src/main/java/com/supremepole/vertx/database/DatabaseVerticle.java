package com.supremepole.vertx.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;

public class DatabaseVerticle extends AbstractVerticle {
    
    private JDBCClient jdbcClient;
    private RedisAPI redisApi;
    
    @Override
    public void start(Promise<Void> startPromise) {
        // 配置 JDBC 客户端（使用 H2 内存数据库）
        JsonObject jdbcConfig = new JsonObject()
            .put("url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
            .put("driver_class", "org.h2.Driver")
            .put("max_pool_size", 30);
        
        jdbcClient = JDBCClient.createShared(vertx, jdbcConfig);
        
        // 配置 Redis 客户端
        Redis.createClient(vertx, new JsonObject()
            .put("host", "localhost")
            .put("port", 6379))
            .connect().onComplete(ar -> {
                if (ar.succeeded()) {
                    redisApi = RedisAPI.api(ar.result());
                    System.out.println("Redis 连接成功");
                } else {
                    System.out.println("Redis 连接失败: " + ar.cause().getMessage());
                }
            });
        
        // 初始化数据库表
        initDatabase().onComplete(ar -> {
            if (ar.succeeded()) {
                setupRoutes();
                startPromise.complete();
            } else {
                startPromise.fail(ar.cause());
            }
        });
    }
    
    private Promise<Void> initDatabase() {
        Promise<Void> promise = Promise.promise();
        
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "email VARCHAR(100) UNIQUE NOT NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        
        jdbcClient.execute(createTableSQL, ar -> {
            if (ar.succeeded()) {
                System.out.println("数据库表初始化成功");
                promise.complete();
            } else {
                promise.fail(ar.cause());
            }
        });
        
        return promise.future();
    }
    
    private void setupRoutes() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        
        // JDBC 示例路由
        router.post("/jdbc/users").handler(this::createUser);
        router.get("/jdbc/users").handler(this::getUsers);
        router.get("/jdbc/users/:id").handler(this::getUserById);
        router.put("/jdbc/users/:id").handler(this::updateUser);
        router.delete("/jdbc/users/:id").handler(this::deleteUser);
        
        // Redis 示例路由
        router.post("/redis/set").handler(this::redisSet);
        router.get("/redis/get/:key").handler(this::redisGet);
        router.delete("/redis/del/:key").handler(this::redisDel);
        router.post("/redis/hset").handler(this::redisHSet);
        router.get("/redis/hget/:key/:field").handler(this::redisHGet);
        
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080, ar -> {
                if (ar.succeeded()) {
                    System.out.println("Database Server started on port 8080");
                }
            });
    }
    
    private void createUser(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String name = body.getString("name");
        String email = body.getString("email");
        
        jdbcClient.updateWithParams(
            "INSERT INTO users (name, email) VALUES (?, ?)",
            new JsonArray().add(name).add(email),
            ar -> {
                if (ar.succeeded()) {
                    ctx.response()
                        .setStatusCode(201)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject()
                            .put("id", ar.result().getKeys().get(0))
                            .put("name", name)
                            .put("email", email)
                            .toString());
                } else {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", ar.cause().getMessage()).toString());
                }
            }
        );
    }
    
    private void getUsers(RoutingContext ctx) {
        jdbcClient.query("SELECT * FROM users", ar -> {
            if (ar.succeeded()) {
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonArray(ar.result().getRows()).toString());
            } else {
                ctx.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("error", ar.cause().getMessage()).toString());
            }
        });
    }
    
    private void getUserById(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        jdbcClient.queryWithParams(
            "SELECT * FROM users WHERE id = ?",
            new JsonArray().add(id),
            ar -> {
                if (ar.succeeded()) {
                    if (ar.result().getRows().isEmpty()) {
                        ctx.response().setStatusCode(404).end();
                    } else {
                        ctx.response()
                            .putHeader("content-type", "application/json")
                            .end(ar.result().getRows().get(0).toString());
                    }
                } else {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", ar.cause().getMessage()).toString());
                }
            }
        );
    }
    
    private void updateUser(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        JsonObject body = ctx.getBodyAsJson();
        
        jdbcClient.updateWithParams(
            "UPDATE users SET name = ?, email = ? WHERE id = ?",
            new JsonArray().add(body.getString("name")).add(body.getString("email")).add(id),
            ar -> {
                if (ar.succeeded()) {
                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("updated", ar.result().getUpdated()).toString());
                } else {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", ar.cause().getMessage()).toString());
                }
            }
        );
    }
    
    private void deleteUser(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        jdbcClient.updateWithParams(
            "DELETE FROM users WHERE id = ?",
            new JsonArray().add(id),
            ar -> {
                if (ar.succeeded()) {
                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("deleted", ar.result().getUpdated()).toString());
                } else {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", ar.cause().getMessage()).toString());
                }
            }
        );
    }
    
    private void redisSet(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String key = body.getString("key");
        String value = body.getString("value");
        
        if (redisApi != null) {
            redisApi.set(key, value, ar -> {
                if (ar.succeeded()) {
                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("success", true).toString());
                } else {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", ar.cause().getMessage()).toString());
                }
            });
        } else {
            ctx.response()
                .setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", "Redis 未连接").toString());
        }
    }
    
    private void redisGet(RoutingContext ctx) {
        String key = ctx.pathParam("key");
        
        if (redisApi != null) {
            redisApi.get(key, ar -> {
                if (ar.succeeded()) {
                    String value = ar.result() != null ? ar.result().toString() : null;
                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("key", key).put("value", value).toString());
                } else {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", ar.cause().getMessage()).toString());
                }
            });
        } else {
            ctx.response()
                .setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", "Redis 未连接").toString());
        }
    }
    
    private void redisDel(RoutingContext ctx) {
        String key = ctx.pathParam("key");
        
        if (redisApi != null) {
            redisApi.del(key, ar -> {
                if (ar.succeeded()) {
                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("deleted", ar.result()).toString());
                } else {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", ar.cause().getMessage()).toString());
                }
            });
        } else {
            ctx.response()
                .setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", "Redis 未连接").toString());
        }
    }
    
    private void redisHSet(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String key = body.getString("key");
        String field = body.getString("field");
        String value = body.getString("value");
        
        if (redisApi != null) {
            redisApi.hset(key, field, value, ar -> {
                if (ar.succeeded()) {
                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("success", true).toString());
                } else {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", ar.cause().getMessage()).toString());
                }
            });
        } else {
            ctx.response()
                .setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", "Redis 未连接").toString());
        }
    }
    
    private void redisHGet(RoutingContext ctx) {
        String key = ctx.pathParam("key");
        String field = ctx.pathParam("field");
        
        if (redisApi != null) {
            redisApi.hget(key, field, ar -> {
                if (ar.succeeded()) {
                    String value = ar.result() != null ? ar.result().toString() : null;
                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("key", key).put("field", field).put("value", value).toString());
                } else {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", ar.cause().getMessage()).toString());
                }
            });
        } else {
            ctx.response()
                .setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", "Redis 未连接").toString());
        }
    }
}