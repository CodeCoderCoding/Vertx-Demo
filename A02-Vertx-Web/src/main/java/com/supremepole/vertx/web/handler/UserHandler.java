package com.supremepole.vertx.web.handler;

import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class UserHandler {
    private List<JsonObject> users;

    public UserHandler() {
        users = new ArrayList<>();
        // init some test data
        users.add(new JsonObject().put("id", 1).put("name", "Alice"));
        users.add(new JsonObject().put("id", 2).put("name", "Bob"));
    }

    public void getAllUsers(RoutingContext context) {
        JsonArray userArray = new JsonArray(users);
        context.response()
                .putHeader("content-type", "application/json")
                .end(userArray.encode());
    }

    public void getUserById(RoutingContext context) {
        int id = Integer.parseInt(context.request().getParam("id"));
        JsonObject user = users.stream()
                .filter(u -> u.getInteger("id").equals(id))
                .findFirst()
                .orElse(null);

        if (user != null) {
            context.response()
                    .putHeader("content-type", "application/json")
                    .end(user.encode());
        } else {
            context.response()
                    .setStatusCode(404)
                    .end("User not found");
        }
    }

    public void createUser(RoutingContext context) {
        context.request().bodyHandler(body -> {
            JsonObject newUser = body.toJsonObject();
            int newId = users.size() + 1;
            newUser.put("id", newId);
            users.add(newUser);

            context.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json")
                    .end(newUser.encode());
        });
    }
}