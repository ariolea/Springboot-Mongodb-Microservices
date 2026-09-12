package com.tvmaze.config;

import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ajustes del cliente de MongoDB.
 *
 * <p>El driver espera 30 segundos por defecto a que haya un servidor disponible. Con un
 * limite mas corto, una base caida se reporta como 503 en unos segundos en lugar de
 * mantener la peticion colgada.
 */
@Configuration
public class MongoConfig {

    private static final long SERVER_SELECTION_TIMEOUT_SECONDS = 5;

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoTimeoutCustomizer() {
        return builder -> builder.applyToClusterSettings(cluster ->
                cluster.serverSelectionTimeout(SERVER_SELECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }
}
