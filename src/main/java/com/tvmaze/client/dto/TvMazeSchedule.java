package com.tvmaze.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Horario de transmision del show. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TvMazeSchedule(String time, List<String> days) {
}
