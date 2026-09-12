package com.tvmaze.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parametros configurables del consumo del API de TVmaze (prefijo {@code tvmaze} en application.yml).
 *
 * @param baseUrl        URL base del API publico de TVmaze.
 * @param connectTimeout tiempo maximo para establecer la conexion con TVmaze.
 * @param readTimeout    tiempo maximo de espera por la respuesta de TVmaze.
 * @param cache          configuracion de la cache local de respuestas.
 */
@ConfigurationProperties(prefix = "tvmaze")
public record TvMazeProperties(

        @DefaultValue("https://api.tvmaze.com") String baseUrl,

        @DefaultValue("3s") Duration connectTimeout,

        @DefaultValue("10s") Duration readTimeout,

        @DefaultValue Cache cache) {

    /**
     * @param maximumSize numero maximo de entradas por cache.
     * @param timeToLive  vigencia de cada entrada a partir de su escritura.
     */
    public record Cache(
            @DefaultValue("500") long maximumSize,
            @DefaultValue("10m") Duration timeToLive) {
    }
}
