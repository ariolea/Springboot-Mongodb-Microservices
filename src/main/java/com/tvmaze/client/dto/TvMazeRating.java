package com.tvmaze.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Calificacion promedio del show en TVmaze. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TvMazeRating(Double average) {
}
