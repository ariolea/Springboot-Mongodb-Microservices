package com.tvmaze.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Objeto show completo tal como lo publica TVmaze en
 * {@code https://api.tvmaze.com/shows/{show_id}}.
 *
 * <p>Se propaga integro al consumidor del middleware; los campos desconocidos
 * se ignoran para que un cambio aditivo en TVmaze no rompa este servicio.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TvMazeShow(
        Long id,
        String url,
        String name,
        String type,
        String language,
        List<String> genres,
        String status,
        Integer runtime,
        Integer averageRuntime,
        String premiered,
        String ended,
        String officialSite,
        TvMazeSchedule schedule,
        TvMazeRating rating,
        Integer weight,
        TvMazeNetwork network,
        TvMazeWebChannel webChannel,
        String dvdCountry,
        TvMazeExternals externals,
        TvMazeImage image,
        String summary,
        Long updated,
        @JsonProperty("_links") TvMazeLinks links) {
}
