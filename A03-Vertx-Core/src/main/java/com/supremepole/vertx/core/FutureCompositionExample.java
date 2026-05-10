package com.supremepole.vertx.core;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class FutureCompositionExample {

    public static void runExample(Vertx vertx) {
        System.out.println("\n=== Future 组合操作示例 ===");

        // 1. 用户注册完整流程
        userRegistrationFlow(vertx);

        // 2. compose 链式调用
        composeChainExample(vertx);

        // 3. map 转换示例
        mapTransformExample();

        // 4. recover 错误恢复示例
        recoverExample();

        // 5. otherwise 默认值示例
        otherwiseExample();

        // 6. flatMap 展平示例
        flatMapExample();
    }

    /**
     * 用户注册完整流程示例
     * 验证邮箱 → 检查是否存在 → 加密密码 → 创建用户 → 发送邮件
     */
    private static void userRegistrationFlow(Vertx vertx) {
        System.out.println("\n--- 1. 用户注册完整流程 ---");

        JsonObject registration = new JsonObject()
            .put("email", "test@example.com")
            .put("password", "password123");

        registerUser(vertx, registration)
            .onSuccess(user -> System.out.println("注册成功: " + user.encode()))
            .onFailure(err -> System.err.println("注册失败: " + err.getMessage()));
    }

    private static Future<JsonObject> registerUser(Vertx vertx, JsonObject registration) {
        String email = registration.getString("email");
        String password = registration.getString("password");

        return validateEmail(email)
            .compose(valid -> {
                if (!valid) {
                    return Future.failedFuture("邮箱格式无效");
                }
                return checkEmailExists(vertx, email);
            })
            .compose(exists -> {
                if (exists) {
                    return Future.failedFuture("邮箱已被注册");
                }
                return hashPassword(password);
            })
            .compose(hashed -> createUser(vertx, email, hashed))
            .compose(user -> sendWelcomeEmail(vertx, user)
                .map(user)); // 保持 User 类型
    }

    private static Future<Boolean> validateEmail(String email) {
        boolean valid = email != null && email.contains("@");
        return Future.succeededFuture(valid);
    }

    private static Future<Boolean> checkEmailExists(Vertx vertx, String email) {
        Promise<Boolean> promise = Promise.promise();
        vertx.setTimer(200, id -> promise.complete(false)); // 模拟查询，返回不存在
        return promise.future();
    }

    private static Future<String> hashPassword(String password) {
        Promise<String> promise = Promise.promise();
        vertx.setTimer(300, id -> promise.complete("hashed_" + password));
        return promise.future();
    }

    private static Future<JsonObject> createUser(Vertx vertx, String email, String hashedPassword) {
        Promise<JsonObject> promise = Promise.promise();
        vertx.setTimer(200, id -> {
            JsonObject user = new JsonObject()
                .put("id", 123)
                .put("email", email)
                .put("password", hashedPassword);
            promise.complete(user);
        });
        return promise.future();
    }

    private static Future<Void> sendWelcomeEmail(Vertx vertx, JsonObject user) {
        Promise<Void> promise = Promise.promise();
        vertx.setTimer(300, id -> {
            System.out.println("发送欢迎邮件给: " + user.getString("email"));
            promise.complete();
        });
        return promise.future();
    }

    /**
     * compose 链式调用示例
     */
    private static void composeChainExample(Vertx vertx) {
        System.out.println("\n--- 2. compose 链式调用 ---");

        Future<Integer> future1 = Future.succeededFuture(10);

        future1.compose(result -> {
            System.out.println("第一步结果: " + result);
            return Future.succeededFuture(result * 2);
        }).compose(result -> {
            System.out.println("第二步结果: " + result);
            return Future.succeededFuture(result + 5);
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("最终结果: " + ar.result());
            }
        });
    }

    /**
     * map 转换示例
     */
    private static void mapTransformExample() {
        System.out.println("\n--- 3. map 转换 ---");

        Future<Integer> future = Future.succeededFuture(5);

        future.map(num -> num * 10)
              .map(num -> "结果: " + num)
              .onSuccess(result -> System.out.println("map 链式转换: " + result));
    }

    /**
     * recover 错误恢复示例
     */
    private static void recoverExample() {
        System.out.println("\n--- 4. recover 错误恢复 ---");

        Future<String> failedFuture = Future.failedFuture(new RuntimeException("出错了"));

        failedFuture.recover(cause -> {
            System.out.println("捕获错误: " + cause.getMessage());
            return Future.succeededFuture("恢复后的默认值");
        }).onSuccess(result -> System.out.println("recover 结果: " + result));
    }

    /**
     * otherwise 默认值示例
     */
    private static void otherwiseExample() {
        System.out.println("\n--- 5. otherwise 默认值 ---");

        Future<Integer> maybeFailed = Future.failedFuture(new RuntimeException("失败"));
        maybeFailed.otherwise(0).onSuccess(result -> {
            System.out.println("otherwise 默认值: " + result);
        });
    }

    /**
     * flatMap 展平示例
     */
    private static void flatMapExample() {
        System.out.println("\n--- 6. flatMap 展平 ---");

        Future<String> future = Future.succeededFuture("Hello");

        future.flatMap(s -> {
            // 返回 Future<String>，flatMap 会自动展平
            return Future.succeededFuture(s + " World");
        }).onSuccess(result -> {
            System.out.println("flatMap 结果: " + result);
        });
    }
}