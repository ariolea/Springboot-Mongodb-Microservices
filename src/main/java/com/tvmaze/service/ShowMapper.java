package com.tvmaze.service;

import com.tvmaze.client.dto.TvMazeShow;
import com.tvmaze.web.dto.CommentSummary;
import com.tvmaze.web.dto.ShowSearchResponse;
import java.util.List;
import org.springframework.util.StringUtils;

/**
 * Traduce el modelo de TVmaze al contrato expuesto por este middleware.
 */
public final class ShowMapper {

    private ShowMapper() {
    }

    /**
     * Reduce un show de TVmaze a los atributos solicitados por el endpoint de busqueda
     * y le agrega los comentarios guardados para ese show.
     */
    public static ShowSearchResponse toSearchResponse(TvMazeShow show, List<CommentSummary> comments) {
        return new ShowSearchResponse(
                show.id(),
                show.name(),
                resolveChannel(show),
                show.summary(),
                show.genres() != null ? List.copyOf(show.genres()) : List.of(),
                comments != null ? List.copyOf(comments) : List.of());
    }

    /**
     * El canal es el nombre de la cadena tradicional y, cuando el show solo existe
     * en streaming, el nombre de la plataforma. Devuelve {@code null} si TVmaze no
     * reporta ninguno de los dos.
     */
    static String resolveChannel(TvMazeShow show) {
        if (show.network() != null && StringUtils.hasText(show.network().name())) {
            return show.network().name();
        }
        if (show.webChannel() != null && StringUtils.hasText(show.webChannel().name())) {
            return show.webChannel().name();
        }
        return null;
    }
}
