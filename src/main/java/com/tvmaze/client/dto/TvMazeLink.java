package com.tvmaze.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Enlace individual dentro de {@code _links}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TvMazeLink(String href, String name) {
}
