package com.consumer;

import com.utils.Constants;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class ConsumerVerticle extends AbstractVerticle {

    private MongoClient mongo;
    private static final String COLLECTION_NAME = "dogs";
    private static final Logger logger = LoggerFactory.getLogger(ConsumerVerticle.class);

    @Override
    public void start(Future future) {
        logger.info("Starting ConsumerVerticle");
        JsonObject config = new JsonObject().put("host", "127.0.0.1");
        mongo = MongoClient.createNonShared(vertx, config);
        //create a handler for POST requests
        MessageConsumer<JsonObject> msgConsumer = vertx.eventBus().consumer(Constants.POST_ADDRESS);
        msgConsumer.handler(json -> {
            System.out.println("Consumer: a message received: " + json.body());
            insertIntoCollection(json.body());
            json.reply("ACK from Consumer.");
        });

        vertx.eventBus().consumer(Constants.GET_ADDRESS, msg -> {
            getAll();
            logger.info("Consumer: a GET request received.");
            msg.reply("ACK from Consumer.");
        });

        future.complete();
    }

    private void insertIntoCollection(JsonObject json) {
        mongo.findOne(COLLECTION_NAME, json, null, res -> {
            if (res.succeeded()) {
                if (res.result() != null) {
                    System.out.println("Entry found, will not insert.");
                } else {
                    mongo.insert(COLLECTION_NAME, json, r -> {
                        if (r.succeeded()) {
                            System.out.println("Successfully inserted entry.");
                            //logger.info("Successfully inserted entry.");
                        } else {
                            System.out.println("Failed to insert entry.");
                            r.cause().printStackTrace();
                        }
                    });
                }
            } else {
                res.cause().printStackTrace();
            }
        });
    }

    private void getAll() {
        mongo.find(COLLECTION_NAME, new JsonObject(), res -> {
            if (res.succeeded()) {
                JsonArray array = new JsonArray(res.result());
                vertx.eventBus().send(Constants.GET_ADDRESS, array, reply -> {
                    if (reply.succeeded()) {
                        System.out.println("ConsumerVerticle mongo: received a reply: " + reply.result().body());
                    } else {
                        System.out.println("ConsumerVerticle: failed to receive a reply.");
                        reply.cause().printStackTrace();
                    }
                });
            } else {
                res.cause().printStackTrace();
                logger.info("Failed to get Mongo records.");
            }
        });
    }

    //retrieve all records from mongo db
    /*private void getAll() {
        mongo.find(COLLECTION_NAME, new JsonObject(), res -> {
            if (res.succeeded()) {
                dogs = new JsonArray(res.result());
                //return;
            } else {
                logger.info("Failed to retrieve results from Mongo.");
                res.cause().printStackTrace();
            }
        });
    }*/

}

