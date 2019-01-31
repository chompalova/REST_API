package com.restserver;

import io.vertx.core.Vertx;

public class ServiceLauncher {

    //private static final Logger logger = LoggerFactory.getLogger(com.restserver.HttpServerVerticle.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(VerticleContainer.class.getName());
        System.out.println("Successfully deployed verticle container.");
    }
}
