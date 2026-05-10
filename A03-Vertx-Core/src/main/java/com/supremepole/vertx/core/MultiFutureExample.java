package com.supremepole.vertx.core;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MultiFutureExample {

    public static void runExample(Vertx vertx) {
        System.out.println("\n=== Multi-Future 操作示例 ===");

        // 1. 并行查询 + 结果合并（Dashboard 场景）
        parallelQueryExample(vertx);

        // 2. all - 等待所有成功
        allExample(vertx);

        // 3. join - 等待全部完成（无论成功失败）
        joinExample(vertx);

        // 4. any - 任一成功即可
        anyExample(vertx);

        // 5. race - 竞速模式
        raceExample(vertx);

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

        // 使用自定义 all 实现，需要将不同类型的 Future 转换为 Future<Object>
        all(
            userFuture.map(o -> (Object) o),
            notifCountFuture.map(o -> (Object) o),
            recentOrdersFuture.map(o -> (Object) o)
        )
            .onComplete(ar -> {
                if (ar.succeeded()) {
                    List<Object> results = ar.result();
                    JsonObject user = (JsonObject) results.get(0);
                    int notifCount = (Integer) results.get(1);
                    JsonArray orders = (JsonArray) results.get(2);

                    JsonObject dashboard = new JsonObject()
                        .put("user", user)
                        .put("notificationCount", notifCount)
                        .put("recentOrders", orders);
                    System.out.println("Dashboard 加载完成: " + dashboard.encode());
                } else {
                    System.err.println("Dashboard 加载失败: " + ar.cause().getMessage());
                }
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
     * all 示例 - 等待所有 Future 成功，任一失败则整体失败
     */
    private static void allExample(Vertx vertx) {
        System.out.println("\n--- 2. Future.all ---");

        Future<String> f1 = delayedSuccess(vertx, "A", 200);
        Future<String> f2 = delayedSuccess(vertx, "B", 300);
        Future<String> f3 = delayedSuccess(vertx, "C", 100);

        all(f1, f2, f3).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("Future.all 成功 - 所有任务完成");
                List<String> results = ar.result();
                System.out.println("结果: " + String.join(", ", results));
            } else {
                System.err.println("Future.all 失败: " + ar.cause().getMessage());
            }
        });
    }

    /**
     * join 示例 - 等待所有 Future 完成，即使部分失败
     */
    private static void joinExample(Vertx vertx) {
        System.out.println("\n--- 3. Future.join ---");

        Future<String> f1 = delayedSuccess(vertx, "Result1", 150);
        Future<String> f2 = delayedFailure(vertx, new RuntimeException("失败测试"), 200);
        Future<String> f3 = delayedSuccess(vertx, "Result3", 100);

        join(f1, f2, f3).onComplete(ar -> {
            System.out.println("Future.join 完成");
            List<Future<String>> futures = Arrays.asList(f1, f2, f3);
            for (int i = 0; i < futures.size(); i++) {
                if (futures.get(i).succeeded()) {
                    System.out.println("Future " + i + " 成功: " + futures.get(i).result());
                } else {
                    System.out.println("Future " + i + " 失败");
                }
            }
        });
    }

    /**
     * any 示例 - 任一 Future 成功即成功
     */
    private static void anyExample(Vertx vertx) {
        System.out.println("\n--- 4. Future.any ---");

        Future<String> f1 = delayedSuccess(vertx, "Fast", 100);
        Future<String> f2 = delayedSuccess(vertx, "Slow", 500);

        any(f1, f2).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("Future.any 成功");
            }
        });
    }

    /**
     * race 示例 - 竞速模式，第一个完成的（成功或失败）
     */
    private static void raceExample(Vertx vertx) {
        System.out.println("\n--- 5. race 竞速模式 ---");

        Future<String> f1 = delayedSuccess(vertx, "Winner", 200);
        Future<String> f2 = delayedFailure(vertx, new RuntimeException("超时"), 300);

        race(f1, f2).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("race 成功: " + ar.result());
            } else {
                System.out.println("race 失败: " + ar.cause().getMessage());
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

        return join(sendFutures)
            .map(results -> {
                int success = 0;
                int failed = 0;
                for (int i = 0; i < userIds.size(); i++) {
                    if (sendFutures.get(i).succeeded()) success++;
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

    // 自定义 all 实现 - 等待所有成功
    @SafeVarargs
    private static <T> Future<List<T>> all(Future<T>... futures) {
        return all(Arrays.asList(futures));
    }

    private static <T> Future<List<T>> all(List<Future<T>> futures) {
        if (futures.isEmpty()) {
            return Future.succeededFuture(new ArrayList<>());
        }

        Promise<List<T>> promise = Promise.promise();
        List<T> results = new ArrayList<>(futures.size());
        int[] completed = {0};

        for (int i = 0; i < futures.size(); i++) {
            final int index = i;
            futures.get(i).onComplete(ar -> {
                if (ar.failed()) {
                    promise.tryFail(ar.cause());
                    return;
                }
                
                // 保持顺序
                synchronized (results) {
                    while (results.size() <= index) {
                        results.add(null);
                    }
                    results.set(index, ar.result());
                }
                
                completed[0]++;
                if (completed[0] == futures.size()) {
                    promise.complete(results);
                }
            });
        }

        return promise.future();
    }

    // 自定义 join 实现 - 等待所有完成
    @SafeVarargs
    private static <T> Future<List<T>> join(Future<T>... futures) {
        return join(Arrays.asList(futures));
    }

    private static <T> Future<List<T>> join(List<Future<T>> futures) {
        if (futures.isEmpty()) {
            return Future.succeededFuture(new ArrayList<>());
        }

        Promise<List<T>> promise = Promise.promise();
        List<T> results = new ArrayList<>(futures.size());
        int[] completed = {0};

        for (int i = 0; i < futures.size(); i++) {
            final int index = i;
            futures.get(i).onComplete(ar -> {
                synchronized (results) {
                    while (results.size() <= index) {
                        results.add(null);
                    }
                    if (ar.succeeded()) {
                        results.set(index, ar.result());
                    }
                }
                
                completed[0]++;
                if (completed[0] == futures.size()) {
                    promise.complete(results);
                }
            });
        }

        return promise.future();
    }

    // 自定义 any 实现 - 任一成功
    @SafeVarargs
    private static <T> Future<T> any(Future<T>... futures) {
        return any(Arrays.asList(futures));
    }

    private static <T> Future<T> any(List<Future<T>> futures) {
        Promise<T> promise = Promise.promise();
        boolean[] completed = {false};

        for (Future<T> f : futures) {
            f.onComplete(ar -> {
                if (!completed[0]) {
                    completed[0] = true;
                    if (ar.succeeded()) {
                        promise.complete(ar.result());
                    }
                }
            });
        }

        return promise.future();
    }

    // 自定义 race 实现 - 竞速
    @SafeVarargs
    private static <T> Future<T> race(Future<T>... futures) {
        return race(Arrays.asList(futures));
    }

    private static <T> Future<T> race(List<Future<T>> futures) {
        Promise<T> promise = Promise.promise();
        boolean[] completed = {false};

        for (Future<T> f : futures) {
            f.onComplete(ar -> {
                if (!completed[0]) {
                    completed[0] = true;
                    if (ar.succeeded()) {
                        promise.complete(ar.result());
                    } else {
                        promise.fail(ar.cause());
                    }
                }
            });
        }

        return promise.future();
    }

    // 辅助方法
    private static Future<String> delayedSuccess(Vertx vertx, String value, long delay) {
        Promise<String> promise = Promise.promise();
        vertx.setTimer(delay, id -> promise.complete(value));
        return promise.future();
    }

    private static Future<String> delayedFailure(Vertx vertx, RuntimeException error, long delay) {
        Promise<String> promise = Promise.promise();
        vertx.setTimer(delay, id -> promise.fail(error));
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