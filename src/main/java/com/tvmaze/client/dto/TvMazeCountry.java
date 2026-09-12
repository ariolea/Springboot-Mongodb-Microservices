package com.tvmaze.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Pais de origen de una cadena o plataforma. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TvMazeCountry(String name, String code, String timezone) {
}
