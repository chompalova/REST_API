package com.restserver;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ServiceLauncher {

    private static final Logger logger = LoggerFactory.getLogger(com.restserver.HttpServerVerticle.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(VerticleContainer.class.getName());
        logger.info("Successfully deployed verticle container.");
    }
}
