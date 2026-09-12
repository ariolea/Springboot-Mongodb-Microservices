package com.tvmaze.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Plataforma de streaming que transmite el show. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TvMazeWebChannel(Long id, String name, TvMazeCountry country, String officialSite) {
}
