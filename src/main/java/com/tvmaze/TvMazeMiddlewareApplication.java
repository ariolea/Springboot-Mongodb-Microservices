package com.tvmaze;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada del API middleware sobre los servicios publicos de TVmaze.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class TvMazeMiddlewareApplication {

    public static void main(String[] args) {
        SpringApplication.run(TvMazeMiddlewareApplication.class, args);
    }
}
