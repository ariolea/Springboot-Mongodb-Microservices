package com.tvmaze.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Elemento del arreglo devuelto por {@code http://api.tvmaze.com/search/shows?q=query}:
 * un puntaje de relevancia y el show asociado.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TvMazeSearchResult(Double score, TvMazeShow show) {
}
