package com.tvmaze.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Identificadores del show en catalogos externos. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TvMazeExternals(Long tvrage, Long thetvdb, String imdb) {
}
