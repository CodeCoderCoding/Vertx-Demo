package com.supremepole.vertx.core;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class HandlerPatterns {

    public static void runExample(Vertx vertx) {
        System.out.println("\n=== Handler 模式与方法引用示例 ===");

        // 1. 静态方法引用
        staticMethodReferenceExample(vertx);

        // 2. 实例方法引用
        instanceMethodReferenceExample(vertx);

        // 3. 特定类型的方法引用
        specificTypeMethodReferenceExample();

        // 4. 构造方法引用
        constructorReferenceExample();

        // 5. Handler 选择指南
        handlerSelectionGuide();
    }

    /**
     * 静态方法引用示例
     */
    private static void staticMethodReferenceExample(Vertx vertx) {
        System.out.println("\n--- 1. 静态方法引用 ---");

        // 使用静态方法引用
        vertx.createHttpServer()
            .requestHandler(StaticHandlers::handleRequest)
            .listen(8083, ar -> {
                if (ar.succeeded()) {
                    System.out.println("静态方法引用 Server 启动在端口 8083");
                }
            });
    }

    /**
     * 实例方法引用示例
     */
    private static void instanceMethodReferenceExample(Vertx vertx) {
        System.out.println("\n--- 2. 实例方法引用 ---");

        // 创建处理器实例
        RequestHandler handler = new RequestHandler("实例处理器");

        // 使用实例方法引用
        vertx.createHttpServer()
            .requestHandler(handler::handle)
            .listen(8084, ar -> {
                if (ar.succeeded()) {
                    System.out.println("实例方法引用 Server 启动在端口 8084");
                }
            });

        // 使用 this 引用（在非静态上下文中）
        // vertx.createHttpServer().requestHandler(this::handleRequest);
    }

    /**
     * 特定类型的方法引用示例
     */
    private static void specificTypeMethodReferenceExample() {
        System.out.println("\n--- 3. 特定类型的方法引用 ---");

        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");

        // 使用特定类型的方法引用
        // 等价于: name -> System.out.println(name)
        names.forEach(System.out::println);

        // 另一个例子：字符串转大写
        // 等价于: s -> s.toUpperCase()
        names.stream().map(String::toUpperCase).forEach(System.out::println);
    }

    /**
     * 构造方法引用示例
     */
    private static void constructorReferenceExample() {
        System.out.println("\n--- 4. 构造方法引用 ---");

        // 创建 Supplier
        Supplier<User> userSupplier = User::new;
        User user = userSupplier.get();
        System.out.println("创建用户: " + user.name);

        // 在 Vert.x 中应用：部署 Verticle
        // vertx.deployVerticle(MyVerticle::new, ar -> { ... });
    }

    /**
     * Handler 选择指南
     */
    private static void handlerSelectionGuide() {
        System.out.println("\n--- 5. Handler 使用模式速查 ---");
        System.out.println("| 场景 | 推荐写法 | 原因 |");
        System.out.println("|------|---------|------|");
        System.out.println("| 简单单行操作 | Lambda | 简洁 |");
        System.out.println("| 多行操作 | Lambda（多行） | 可读性好 |");
        System.out.println("| 可复用逻辑 | 方法引用 | 代码复用 |");
        System.out.println("| 需要访问 this | this::methodName | 直接访问实例成员 |");
        System.out.println("| 需要类型显式 | 匿名类 | IDE 提示更好（不推荐） |");
    }

    // 静态处理器类
    public static class StaticHandlers {
        public static void handleRequest(HttpServerRequest request) {
            request.response()
                .putHeader("content-type", "text/plain")
                .end("静态方法引用处理");
        }
    }

    // 实例处理器类
    public static class RequestHandler {
        private String name;

        public RequestHandler(String name) {
            this.name = name;
        }

        public void handle(HttpServerRequest request) {
            request.response()
                .putHeader("content-type", "text/plain")
                .end(name + " 处理请求");
        }
    }

    // 用户类（用于构造方法引用示例）
    public static class User {
        String name = "默认用户";

        public User() {}

        public User(String name) {
            this.name = name;
        }
    }
}