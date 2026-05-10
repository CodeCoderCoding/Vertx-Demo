package com.supremepole.vertx.core;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MultiFutureExample {

    public static void runExample(Vertx vertx) {
        System.out.println("\n=== Multi-Future 操作示例 ===");

        // 1. 并行查询 + 结果合并（Dashboard 场景）
        parallelQueryExample(vertx);

        // 2. CompositeFuture.all - 等待所有成功
        compositeAllExample(vertx);

        // 3. CompositeFuture.join - 等待全部完成（无论成功失败）
        compositeJoinExample(vertx);

        // 4. CompositeFuture.any - 任一成功即可
        compositeAnyExample(vertx);

        // 5. CompositeFuture.race - 竞速模式
        compositeRaceExample(vertx);

        // 6. 批量操作 + 部分失败处理
        batchOperationExample(vertx);
    }

    /**
     * 并行查询 + 结果合并示例
     * 首页需要同时加载用户信息、通知数、最近订单
     */
    private static void parallelQueryExample(Vertx vertx) {
        System.out.println("\n--- 1. 并行查询 + 结果合并 ---");

        int userId = 1;
        Future<JsonObject> userFuture = fetchUser(vertx, userId);
        Future<Integer> notifCountFuture = fetchNotificationCount(vertx, userId);
        Future<JsonArray> recentOrdersFuture = fetchRecentOrders(vertx, userId);

        CompositeFuture.all(userFuture, notifCountFuture, recentOrdersFuture)
            .map(composite -> {
                JsonObject user = composite.resultAt(0);
                int notifCount = composite.resultAt(1);
                JsonArray orders = composite.resultAt(2);

                return new JsonObject()
                    .put("user", user)
                    .put("notificationCount", notifCount)
                    .put("recentOrders", orders);
            })
            .onSuccess(dashboard -> {
                System.out.println("Dashboard 加载完成: " + dashboard.encode());
            })
            .onFailure(err -> {
                System.err.println("Dashboard 加载失败: " + err.getMessage());
            });
    }

    private static Future<JsonObject> fetchUser(Vertx vertx, int userId) {
        Promise<JsonObject> promise = Promise.promise();
        vertx.setTimer(300, id -> {
            promise.complete(new JsonObject()
                .put("id", userId)
                .put("name", "张三")
                .put("email", "zhangsan@example.com"));
        });
        return promise.future();
    }

    private static Future<Integer> fetchNotificationCount(Vertx vertx, int userId) {
        Promise<Integer> promise = Promise.promise();
        vertx.setTimer(200, id -> promise.complete(5));
        return promise.future();
    }

    private static Future<JsonArray> fetchRecentOrders(Vertx vertx, int userId) {
        Promise<JsonArray> promise = Promise.promise();
        vertx.setTimer(400, id -> {
            promise.complete(new JsonArray()
                .add(new JsonObject().put("id", "ORD001").put("amount", 100))
                .add(new JsonObject().put("id", "ORD002").put("amount", 200)));
        });
        return promise.future();
    }

    /**
     * CompositeFuture.all 示例
     * 等待所有 Future 成功，任一失败则整体失败
     */
    private static void compositeAllExample(Vertx vertx) {
        System.out.println("\n--- 2. CompositeFuture.all ---");

        Future<String> f1 = delayedSuccess("A", 200);
        Future<String> f2 = delayedSuccess("B", 300);
        Future<String> f3 = delayedSuccess("C", 100);

        CompositeFuture.all(f1, f2, f3).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("CompositeFuture.all 成功 - 所有任务完成");
                // 获取各个 Future 的结果
                String result1 = ar.result().resultAt(0);
                String result2 = ar.result().resultAt(1);
                String result3 = ar.result().resultAt(2);
                System.out.println("结果: " + result1 + ", " + result2 + ", " + result3);
            } else {
                System.err.println("CompositeFuture.all 失败: " + ar.cause().getMessage());
            }
        });
    }

    /**
     * CompositeFuture.join 示例
     * 等待所有 Future 完成，即使部分失败
     */
    private static void compositeJoinExample(Vertx vertx) {
        System.out.println("\n--- 3. CompositeFuture.join ---");

        List<Future<String>> futures = Arrays.asList(
            delayedSuccess("Result1", 150),
            delayedFailure(new RuntimeException("失败测试"), 200),
            delayedSuccess("Result3", 100)
        );

        CompositeFuture.join(futures).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("CompositeFuture.join 完成");
                // 检查每个 Future 的状态
                for (int i = 0; i < futures.size(); i++) {
                    if (ar.result().succeeded(i)) {
                        System.out.println("Future " + i + " 成功: " + ar.result().resultAt(i));
                    } else {
                        System.out.println("Future " + i + " 失败");
                    }
                }
            }
        });
    }

    /**
     * CompositeFuture.any 示例
     * 任一 Future 成功即成功
     */
    private static void compositeAnyExample(Vertx vertx) {
        System.out.println("\n--- 4. CompositeFuture.any ---");

        CompositeFuture.any(
            delayedSuccess("Fast", 100),
            delayedSuccess("Slow", 500)
        ).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("CompositeFuture.any 成功: " + ar.result().resultAt(0));
            }
        });
    }

    /**
     * CompositeFuture.race 示例
     * 竞速模式，第一个完成的（成功或失败）
     */
    private static void compositeRaceExample(Vertx vertx) {
        System.out.println("\n--- 5. CompositeFuture.race ---");

        CompositeFuture.race(
            delayedSuccess("Winner", 200),
            delayedFailure(new RuntimeException("超时"), 300)
        ).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("CompositeFuture.race 成功: " + ar.result().resultAt(0));
            } else {
                System.out.println("CompositeFuture.race 失败: " + ar.cause().getMessage());
            }
        });
    }

    /**
     * 批量操作 + 部分失败处理示例
     */
    private static void batchOperationExample(Vertx vertx) {
        System.out.println("\n--- 6. 批量操作 + 部分失败处理 ---");

        List<String> userIds = Arrays.asList("user1", "user2", "user3", "user4");
        String message = "系统通知";

        sendBatchNotifications(vertx, userIds, message)
            .onSuccess(result -> {
                System.out.println("批量通知完成 - 成功: " + result.successCount + ", 失败: " + result.failedCount);
            });
    }

    private static Future<BatchResult> sendBatchNotifications(Vertx vertx, List<String> userIds, String message) {
        List<Future<Void>> sendFutures = userIds.stream()
            .map(userId -> sendNotification(vertx, userId, message)
                .recover(err -> {
                    // 单个通知失败不影响其他
                    System.out.println("发送通知给 " + userId + " 失败: " + err.getMessage());
                    return Future.succeededFuture(); // 将失败转为成功
                }))
            .collect(Collectors.toList());

        return CompositeFuture.join(sendFutures)
            .map(composite -> {
                int success = 0;
                int failed = 0;
                for (int i = 0; i < userIds.size(); i++) {
                    if (composite.succeeded(i)) success++;
                    else failed++;
                }
                return new BatchResult(success, failed);
            });
    }

    private static Future<Void> sendNotification(Vertx vertx, String userId, String message) {
        Promise<Void> promise = Promise.promise();
        // 模拟部分失败
        if ("user3".equals(userId)) {
            vertx.setTimer(100, id -> promise.fail(new RuntimeException("用户不存在")));
        } else {
            vertx.setTimer(100, id -> {
                System.out.println("发送通知给 " + userId + ": " + message);
                promise.complete();
            });
        }
        return promise.future();
    }

    // 辅助方法
    private static Future<String> delayedSuccess(String value, long delay) {
        Promise<String> promise = Promise.promise();
        Vertx.currentContext().owner().setTimer(delay, id -> promise.complete(value));
        return promise.future();
    }

    private static Future<String> delayedFailure(RuntimeException error, long delay) {
        Promise<String> promise = Promise.promise();
        Vertx.currentContext().owner().setTimer(delay, id -> promise.fail(error));
        return promise.future();
    }

    // 内部类：批量结果
    private static class BatchResult {
        int successCount;
        int failedCount;

        BatchResult(int successCount, int failedCount) {
            this.successCount = successCount;
            this.failedCount = failedCount;
        }
    }
}