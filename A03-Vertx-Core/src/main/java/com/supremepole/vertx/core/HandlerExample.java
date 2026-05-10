package com.supremepole.vertx.core;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;

public class HandlerExample {

    public static void runExample(Vertx vertx) {
        System.out.println("\n=== Handler 示例 ===");

        // 1. Handler<HttpServerRequest> - HTTP 请求处理
        httpRequestHandlerExample(vertx);

        // 2. Handler<AsyncResult<T>> - 异步操作回调
        asyncResultHandlerExample(vertx);

        // 3. Handler<Message<T>> - Event Bus 消息处理
        eventBusHandlerExample(vertx);

        // 4. Handler<Void>/Handler<Long> - 定时器
        timerHandlerExample(vertx);

        // 5. Handler<Buffer> - 数据流处理
        bufferHandlerExample(vertx);

        // 6. Handler 使用方式对比（匿名类/Lambda/方法引用）
        handlerUsageComparison(vertx);
    }

    private static void httpRequestHandlerExample(Vertx vertx) {
        System.out.println("\n--- 1. HTTP 请求处理 Handler ---");
        
        // Lambda 写法（推荐）
        vertx.createHttpServer()
            .requestHandler(request -> {
                String path = request.path();
                request.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello from " + path);
            })
            .listen(8081).onComplete(ar -> {
                if (ar.succeeded()) {
                    System.out.println("HTTP Server started on port 8081");
                }
            });

        // 方法引用写法
        vertx.createHttpServer()
            .requestHandler(HandlerExample::handleRequest)
            .listen(8082).onComplete(ar -> {
                if (ar.succeeded()) {
                    System.out.println("HTTP Server (方法引用) started on port 8082");
                }
            });
    }

    private static void handleRequest(HttpServerRequest request) {
        request.response()
            .putHeader("content-type", "text/plain")
            .end("Handled by method reference");
    }

    private static void asyncResultHandlerExample(Vertx vertx) {
        System.out.println("\n--- 2. AsyncResult Handler ---");

        // 文件读取 - Vertx 5.x 返回 Future
        vertx.fileSystem().readFile("example.txt").onComplete(ar -> {
            if (ar.succeeded()) {
                Buffer buffer = ar.result();
                System.out.println("文件内容长度: " + buffer.length());
            } else {
                System.err.println("读取失败: " + ar.cause().getMessage());
            }
        });

        // 创建文件 - Vertx 5.x 返回 Future
        vertx.fileSystem().createFile("example.txt").onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("文件创建成功");
            } else {
                System.err.println("创建失败: " + ar.cause().getMessage());
            }
        });
    }

    private static void eventBusHandlerExample(Vertx vertx) {
        System.out.println("\n--- 3. Event Bus Handler ---");

        // 注册消息消费者
        vertx.eventBus().consumer("test.topic", message -> {
            String body = message.body().toString();
            System.out.println("收到消息: " + body);
            message.reply("已收到: " + body);
        });

        // 发送消息并等待回复 - Vertx 5.x 返回 Future
        vertx.eventBus().request("test.topic", "Hello Event Bus").onComplete(reply -> {
            if (reply.succeeded()) {
                System.out.println("收到回复: " + reply.result().body());
            } else {
                System.err.println("发送失败: " + reply.cause().getMessage());
            }
        });

        // 使用方法引用
        vertx.eventBus().consumer("order.topic", HandlerExample::handleOrderMessage);
    }

    private static void handleOrderMessage(Message<String> message) {
        System.out.println("订单消息: " + message.body());
        message.reply("订单已处理");
    }

    private static void timerHandlerExample(Vertx vertx) {
        System.out.println("\n--- 4. 定时器 Handler ---");

        // 一次性定时器
        long timerId = vertx.setTimer(1000, id -> {
            System.out.println("1秒后执行，timerId=" + id);
        });

        // 周期性定时器
        long periodicId = vertx.setPeriodic(2000, id -> {
            System.out.println("每2秒执行一次");
        });

        // 取消定时器（演示用）
        vertx.setTimer(5000, id -> {
            vertx.cancelTimer(periodicId);
            System.out.println("周期性定时器已取消");
        });

        // 使用方法引用
        vertx.setTimer(3000, HandlerExample::onTimerFired);
    }

    private static void onTimerFired(Long timerId) {
        System.out.println("方法引用定时器触发: " + timerId);
    }

    private static void bufferHandlerExample(Vertx vertx) {
        System.out.println("\n--- 5. Buffer Handler（数据流处理）---");

        // 模拟数据流转
        StringBuilder receivedData = new StringBuilder();
        
        // 模拟数据块
        Handler<Buffer> dataHandler = buffer -> {
            receivedData.append(buffer.toString());
            System.out.println("收到数据块，长度: " + buffer.length());
        };

        // 模拟结束处理
        Handler<Void> endHandler = v -> {
            System.out.println("数据流结束，总长度: " + receivedData.length());
        };

        // 模拟发送数据
        vertx.setTimer(500, id -> dataHandler.handle(Buffer.buffer("Hello")));
        vertx.setTimer(1000, id -> dataHandler.handle(Buffer.buffer(" ")));
        vertx.setTimer(1500, id -> dataHandler.handle(Buffer.buffer("World")));
        vertx.setTimer(2000, id -> endHandler.handle(null));
    }

    private static void handlerUsageComparison(Vertx vertx) {
        System.out.println("\n--- 6. Handler 使用方式对比 ---");

        // 方式1：匿名内部类（最冗长，但类型明确）
        Handler<String> anonymousHandler = new Handler<String>() {
            @Override
            public void handle(String event) {
                System.out.println("匿名类处理: " + event);
            }
        };
        anonymousHandler.handle("测试消息");

        // 方式2：Lambda（推荐，简洁）
        Handler<String> lambdaHandler = msg -> {
            System.out.println("Lambda 处理: " + msg);
        };
        lambdaHandler.handle("测试消息");

        // 方式3：方法引用（复用逻辑）
        Handler<String> methodRefHandler = HandlerExample::processMessage;
        methodRefHandler.handle("测试消息");
    }

    private static void processMessage(String message) {
        System.out.println("方法引用处理: " + message);
    }
}