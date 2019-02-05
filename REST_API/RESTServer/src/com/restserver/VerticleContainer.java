package com.restserver;

import com.consumer.ConsumerVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


public class VerticleContainer extends AbstractVerticle{ //verticle container

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void start() {
        vertx.deployVerticle(new ConsumerVerticle(), res -> {
            if (res.succeeded()) {
                logger.info("Successfully deployed ConsumerVerticle");
                vertx.deployVerticle(new HttpServerVerticle(), r -> {
                    if (res.succeeded()) {
                        logger.info("Successfully deployed HttpServerVerticle");
                    } else {
                        logger.info("Failed to deploy HttpServerVerticle");
                    }
                });
            } else {
                logger.info("Failed to deploy ConsumerVerticle.");
                res.cause().printStackTrace();
            }
        });

    }
}