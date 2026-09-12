package com.tvmaze.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Imagenes promocionales del show. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TvMazeImage(String medium, String original) {
}
