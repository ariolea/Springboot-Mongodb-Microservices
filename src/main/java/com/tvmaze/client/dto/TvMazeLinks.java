package com.tvmaze.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Enlaces HAL que acompanan al show ({@code _links}). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TvMazeLinks(TvMazeLink self, TvMazeLink previousepisode, TvMazeLink nextepisode) {
}
