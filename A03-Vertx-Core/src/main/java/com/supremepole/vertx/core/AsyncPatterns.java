package com.supremepole.vertx.core;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.TimeoutException;

public class AsyncPatterns {

    public static void runExample(Vertx vertx) {
        System.out.println("\n=== 异步模式示例 ===");

        // 1. 带超时和重试的请求
        timeoutRetryExample(vertx);

        // 2. 缓存优先 + 降级策略
        cacheFallbackExample(vertx);

        // 3. Promise 作为回调适配器
        callbackAdapterExample(vertx);

        // 4. 错误传播与恢复
        errorPropagationExample(vertx);

        // 5. 资源清理模式
        resourceCleanupExample(vertx);
    }

    /**
     * 带超时和重试的请求示例
     */
    private static void timeoutRetryExample(Vertx vertx) {
        System.out.println("\n--- 1. 带超时和重试的请求 ---");

        fetchWithRetry(vertx, "http://example.com/api", 2)
            .onSuccess(data -> System.out.println("获取数据成功: " + data.encode()))
            .onFailure(err -> System.err.println("获取数据失败: " + err.getMessage()));
    }

    private static Future<JsonObject> fetchWithRetry(Vertx vertx, String url, int maxRetries) {
        return fetchWithRetryInternal(vertx, url, maxRetries, 0);
    }

    private static Future<JsonObject> fetchWithRetryInternal(Vertx vertx, String url, int maxRetries, int attempt) {
        Promise<JsonObject> promise = Promise.promise();

        // 模拟 HTTP 请求
        simulateHttpRequest(vertx, url, attempt > 0)
            .onComplete(promise);

        // 超时处理
        vertx.setTimer(1000, id -> {
            promise.tryFail(new TimeoutException("请求超时"));
        });

        // 失败时重试
        return promise.future()
            .recover(err -> {
                if (attempt < maxRetries) {
                    System.out.println("第 " + (attempt + 1) + " 次重试");
                    return delay(vertx, 500L * (attempt + 1))
                        .compose(v -> fetchWithRetryInternal(vertx, url, maxRetries, attempt + 1));
                }
                return Future.failedFuture(err);
            });
    }

    private static Future<JsonObject> simulateHttpRequest(Vertx vertx, String url, boolean shouldSucceed) {
        Promise<JsonObject> promise = Promise.promise();
        vertx.setTimer(300, id -> {
            if (shouldSucceed) {
                promise.complete(new JsonObject().put("data", "success"));
            } else {
                promise.fail(new RuntimeException("模拟失败"));
            }
        });
        return promise.future();
    }

    /**
     * 缓存优先 + 降级策略示例
     */
    private static void cacheFallbackExample(Vertx vertx) {
        System.out.println("\n--- 2. 缓存优先 + 降级策略 ---");

        getUserWithFallback(vertx, 1)
            .onSuccess(user -> System.out.println("获取用户成功: " + user.encode()))
            .onFailure(err -> System.err.println("获取用户失败: " + err.getMessage()));
    }

    private static Future<JsonObject> getUserWithFallback(Vertx vertx, int userId) {
        return getFromCache(vertx, userId)
            .recover(err -> {
                System.out.println("缓存未命中，查询数据库");
                return getFromDatabase(vertx, userId)
                    .recover(dbErr -> {
                        System.out.println("数据库查询失败，使用默认用户");
                        return Future.succeededFuture(createDefaultUser(userId));
                    });
            });
    }

    private static Future<JsonObject> getFromCache(Vertx vertx, int userId) {
        Promise<JsonObject> promise = Promise.promise();
        vertx.setTimer(100, id -> {
            // 模拟缓存未命中
            promise.fail(new RuntimeException("缓存未命中"));
        });
        return promise.future();
    }

    private static Future<JsonObject> getFromDatabase(Vertx vertx, int userId) {
        Promise<JsonObject> promise = Promise.promise();
        vertx.setTimer(200, id -> {
            promise.complete(new JsonObject()
                .put("id", userId)
                .put("name", "数据库用户")
                .put("source", "database"));
        });
        return promise.future();
    }

    private static JsonObject createDefaultUser(int userId) {
        return new JsonObject()
            .put("id", userId)
            .put("name", "默认用户")
            .put("source", "default");
    }

    /**
     * Promise 作为回调适配器示例
     */
    private static void callbackAdapterExample(Vertx vertx) {
        System.out.println("\n--- 3. Promise 作为回调适配器 ---");

        // 使用适配后的 Future API
        getUserFuture(vertx, 1)
            .map(user -> user.getString("name"))
            .onSuccess(name -> System.out.println("用户名: " + name));
    }

    // 旧式回调 API
    private static void getUserAsync(Vertx vertx, int id, Handler<AsyncResult<JsonObject>> callback) {
        vertx.setTimer(150, timerId -> {
            JsonObject user = new JsonObject()
                .put("id", id)
                .put("name", "张三");
            callback.handle(Future.succeededFuture(user));
        });
    }

    // 使用 Promise 适配为 Future API
    private static Future<JsonObject> getUserFuture(Vertx vertx, int id) {
        Promise<JsonObject> promise = Promise.promise();
        getUserAsync(vertx, id, ar -> {
            if (ar.succeeded()) {
                promise.complete(ar.result());
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    /**
     * 错误传播与恢复示例
     */
    private static void errorPropagationExample(Vertx vertx) {
        System.out.println("\n--- 4. 错误传播与恢复 ---");

        // 示例1: 错误传播
        Future.succeededFuture(10)
            .compose(num -> {
                if (num > 5) {
                    return Future.failedFuture("数字太大");
                }
                return Future.succeededFuture(num * 2);
            })
            .onSuccess(result -> System.out.println("成功: " + result))
            .onFailure(err -> System.err.println("失败: " + err.getMessage()));

        // 示例2: 多层恢复
        Future.failedFuture(new RuntimeException("第一层失败"))
            .recover(err -> {
                System.out.println("第一层恢复: " + err.getMessage());
                return Future.failedFuture("第二层失败");
            })
            .recover(err -> {
                System.out.println("第二层恢复: " + err.getMessage());
                return Future.succeededFuture("最终成功");
            })
            .onSuccess(result -> System.out.println("最终结果: " + result));
    }

    /**
     * 资源清理模式示例
     */
    private static void resourceCleanupExample(Vertx vertx) {
        System.out.println("\n--- 5. 资源清理模式 ---");

        acquireResource(vertx)
            .compose(resource -> {
                System.out.println("使用资源: " + resource);
                // 模拟操作失败
                return Future.failedFuture("操作失败");
            })
            .recover(err -> {
                System.out.println("清理资源");
                return Future.failedFuture(err);
            })
            .onComplete(ar -> {
                if (ar.succeeded()) {
                    System.out.println("操作成功完成");
                } else {
                    System.out.println("操作失败，但资源已清理");
                }
            });
    }

    private static Future<String> acquireResource(Vertx vertx) {
        Promise<String> promise = Promise.promise();
        vertx.setTimer(100, id -> {
            System.out.println("获取资源");
            promise.complete("resource-123");
        });
        return promise.future();
    }

    /**
     * 延迟执行辅助方法
     */
    private static Future<Void> delay(Vertx vertx, long millis) {
        Promise<Void> promise = Promise.promise();
        vertx.setTimer(millis, id -> promise.complete());
        return promise.future();
    }
}