package com.restserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.RedisDataSource;

public class HttpServerVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final int PORT = 8080;
    private RedisClient redis;


    /*@Override
    public void start(Future future) throws Exception {
        Router router = Router.router(vertx);
        //JsonObject config = new JsonObject().put("host", "127.0.0.1");
        //mongo = MongoClient.createNonShared(vertx, config);

        ServiceDiscovery.create(vertx, serviceDiscovery -> {
            RedisDataSource.getRedisClient(serviceDiscovery, rec -> rec.getName().equals("redis"), ar -> {
                if (ar.failed()) {
                    redis = RedisClient.create(vertx, new RedisOptions().setHost("127.0.0.1").setPort(6381));
                } else {
                    redis = ar.result();
                }

                router.get("/api/dogs").handler(this::getAll);
                router.route("/api/dogs*").handler(BodyHandler.create());
                router.post("/api/dogs").handler(this::addItem);

                vertx.createHttpServer().requestHandler(router::accept).listen(PORT, res -> {
                    if (res.succeeded()) {
                        System.out.println("Server listening on port: " + PORT);
                        logger.info("Server listening on port: \" + PORT");
                        //future.complete();
                    } else {
                        System.out.println("Failed to start server.");
                        //future.fail(res.cause());
                    }
                });
            });
        });
    }*/

    @Override
    public void start(Future future) throws Exception {
        Router router = Router.router(vertx);
        //JsonObject config = new JsonObject().put("host", "127.0.0.1");
        //mongo = MongoClient.createNonShared(vertx, config);

        //redis = RedisClient.create(vertx, new RedisOptions().setHost("192.168.0.11").setPort(1435));

                router.get("/api/dogs").handler(this::getAll);
                router.route("/api/dogs*").handler(BodyHandler.create());
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

    /*private void createData (final String collection, final Object obj) throws JsonProcessingException {
        String dogToJson = ow.writeValueAsString(obj);
        insertIntoCollection("dogs", dogToJson);
        System.out.println("Printing dogs' collection:\n" + dogToJson);
    }*/



    private void addItem (RoutingContext context) {
        Dog dog = Json.decodeValue(context.getBodyAsString(), Dog.class);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String dogToJson = null;
        try {
            dogToJson = ow.writeValueAsString(dog);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        EventBus eventBus = vertx.eventBus();
            eventBus.send("CONSUMER",  new JsonObject(dogToJson), res -> {
                if (res.succeeded()) {
                    System.out.println("HttpServerVerticle: received a reply: " + res.result().body());
                } else {
                    System.out.println("HttpServerVerticle: failed to receive a reply.");
                }
            });

        context.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(dog));
    }

    /*private void addItem(RoutingContext context) {
        String body = context.getBodyAsString();
        if (body != null) {
            final Dog dog = Json.decodeValue(body, Dog.class);
            redis.hset("my-dogs-list", dog.getBreed(), Integer.toString(dog.getAge()), res -> {
                if (res.failed()) {
                    context.fail(res.cause());
                } else {
                    getAll(context);
                }
            });
        } else {
            context.response().setStatusCode(400).end();
        }
    }*/

    /*private void getAll(RoutingContext context) {
        JsonObject jsonObject = new JsonObject();
        mongo.find("dogs", jsonObject, res -> {
            if (res.succeeded()) {
                context.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(res.result()));
            } else {
                res.cause().printStackTrace();
                logger.debug("Failed to get Mongo records.");
            }
        });
    }*/
    private void getAll(RoutingContext context) {
        redis.hgetall("my-dogs-list", res -> {
            if (res.failed()) {
                context.fail(res.cause());
            } else {
                context.response().end(res.result().put("served-by", System.getenv("HOSTNAME")).encode());

            }
        });
    }
}
