package com.restserver;

import com.consumer.ConsumerVerticle;

import io.vertx.core.AbstractVerticle;


public class VerticleContainer extends AbstractVerticle{ //verticle container

    @Override
    public void start() {
        vertx.deployVerticle(new ConsumerVerticle(), res -> {
            if (res.succeeded()) {
                System.out.println("Successfully deployed ConsumerVerticle");
                vertx.deployVerticle(new HttpServerVerticle(), r -> {
                    if (res.succeeded()) {
                        System.out.println("Successfully deployed HttpServerVerticle");
                    } else {
                        System.out.println("Failed to deploy HttpServerVerticle");
                    }
                });
            } else {
                System.out.println("Failed to deploy ConsumerVerticle.");
                res.cause().printStackTrace();
            }
        });

    }
}