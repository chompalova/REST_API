package com.restserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.utils.Constants;

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

public class HttpServerVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final int PORT = 8080;
    public static final String HOST = "0.0.0.0";

    @Override
    public void start(Future future) throws Exception {
        Router router = Router.router(vertx);

        router.get("/api/dogs").handler(this::getAll);
        router.route("/api/dogs*").handler(BodyHandler.create());
        router.post("/api/dogs").handler(this::addItem);

        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router::accept).listen(PORT, "0.0.0.0", res -> {
            if (res.succeeded()) {
                logger.info("Server listening on:" + HOST + ":" + PORT);
                future.complete();
            } else {
                logger.info("Failed to start server.");
                future.fail(res.cause());
            }
        });
    }

    private void addItem(RoutingContext context) {
        final Dog dog = Json.decodeValue(context.getBodyAsString(), Dog.class);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        String dogToJson = null;
        try {
            dogToJson = ow.writeValueAsString(dog);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        vertx.eventBus().send(Constants.POST_ADDRESS, new JsonObject(dogToJson), res -> {
            if (res.succeeded()) {
                logger.info("HttpServerVerticle: received a reply: " + res.result().body());
            } else {
                logger.info("HttpServerVerticle: failed to receive a reply.");
            }
        });
        context.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(dog));
    }

    private void getAll(RoutingContext context) {
        EventBus eventBus = vertx.eventBus();
        eventBus.send(Constants.GET_ADDRESS, null, res -> {
            if (res.succeeded()) {
                logger.info("HttpServerVerticle: received a reply: " + res.result().body());
                context.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(res.result().body()));
            } else {
                logger.info("HttpServerVerticle: failed to receive a reply.");
                context.response().setStatusCode(204);
            }
        });
    }
}