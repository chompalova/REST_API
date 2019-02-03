package com.restserver;

import com.utils.Constants;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.List;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.redis.RedisClient;

public class HttpServerVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final int PORT = 8080;
    //EventBus eventBus = vertx.eventBus();

    @Override
    public void start(Future future) throws Exception {
        Router router = Router.router(vertx);

        router.get("/api/dogs").handler(this::getAll);
        router.post("/api/dogs").handler(this::addItem);

        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router::accept).listen(PORT, res -> {
            if (res.succeeded()) {
                System.out.println("Server listening on port: " + PORT);
                logger.info("Server listening on port: \" + PORT");
                future.complete();
            } else {
                System.out.println("Failed to start server.");
                future.fail(res.cause());
            }
        });
    }

    private void addItem(RoutingContext context) {
        context.request().bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            System.out.println(json);
            if (!json.isEmpty() || json != null) {
                //send the json object over the event bus
                vertx.eventBus().send("CONSUMER", json, res -> {
                    if (res.succeeded()) {
                        System.out.println("HttpServerVerticle: received a reply: " + res.result().body());
                    } else {
                        System.out.println("HttpServerVerticle: failed to receive a reply.");
                    }
                });
                context.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(json));
            } else {
                context.response().setStatusCode(400).end("Error: JSON object cannot be empty or null.");
            }
        });
        /*if (body.length() != 0) {
            Dog dog = Json.decodeValue(body, Dog.class);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String dogToJson = null;
            try {
                dogToJson = ow.writeValueAsString(dog);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }*/
        //sending the json object over the event bus
    }

    private void getAll(RoutingContext context) {
        vertx.eventBus().send(Constants.ADDRESS, null, res -> {
            if (res.succeeded()) {
                MessageConsumer<List<JsonObject>> msgConsumer = vertx.eventBus().consumer(Constants.ADDRESS);
                msgConsumer.handler(jsonList -> {
                    context.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(jsonList.body()));
                    logger.info("HttpServerVerticle: received a reply: " + jsonList.body());
                    jsonList.reply("GET Request/ACK from server");
                });
            } else {
                logger.info("HttpServerVerticle: failed to receive a reply.");
                context.response().setStatusCode(204);
            }
        });
    }


        }
