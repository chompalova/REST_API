package com.restserver;

import com.consumer.ConsumerVerticleGET;

import io.vertx.core.AbstractVerticle;


public class VerticleContainer extends AbstractVerticle{ //verticle container

    @Override
    public void start() {
        vertx.deployVerticle(new ConsumerVerticleGET(), res -> {
            if (res.succeeded()) {
                System.out.println("Successfully deployed ConsumerVerticleGET");
                vertx.deployVerticle(new HttpServerVerticle(), r -> {
                    if (res.succeeded()) {
                        System.out.println("Successfully deployed HttpServerVerticle");
                    } else {
                        System.out.println("Failed to deploy HttpServerVerticle");
                    }
                });
            } else {
                System.out.println("Failed to deploy ConsumerVerticleGET.");
                res.cause().printStackTrace();
            }
        });

    }
}