package com.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class ConsumerVerticle extends AbstractVerticle {

    private MongoClient mongo;
    private static final String COLLECTION_NAME = "dogs";

    @Override
    public void start() {
        JsonObject config = new JsonObject().put("host", "127.0.0.1");
        mongo = MongoClient.createNonShared(vertx, config);

        final EventBus eventBus = vertx.eventBus();
        eventBus.consumer("CONSUMER", msg -> {
            System.out.println("Consumer: a message received: " + msg.body());
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

            String json = null;
            try {
                json = ow.writeValueAsString(msg.body());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            insertIntoCollection(json);

            msg.reply("ACK from Consumer.");
        });
    }

    private void insertIntoCollection(String json) {
        JsonObject jsonObject = new JsonObject(json);
        mongo.findOne(COLLECTION_NAME, jsonObject, null, res -> {
            if (res.succeeded()) {
                if (res.result() != null) {
                    System.out.println("Entry found, will not insert.");
                } else {
                    mongo.insert(COLLECTION_NAME, new JsonObject(json), r -> {
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

}

