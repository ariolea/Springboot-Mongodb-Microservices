package com.tvmaze.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Cadena de television tradicional que transmite el show. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TvMazeNetwork(Long id, String name, TvMazeCountry country, String officialSite) {
}
