package com.tvmaze.service;

import com.tvmaze.client.TvMazeClient;
import com.tvmaze.client.dto.TvMazeSearchResult;
import com.tvmaze.client.dto.TvMazeShow;
import com.tvmaze.web.dto.ShowSearchResponse;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Casos de uso del middleware: buscar shows por criterio y consultar un show por id.
 */
@Service
public class ShowService {

    private static final Logger log = LoggerFactory.getLogger(ShowService.class);

    private final TvMazeClient tvMazeClient;

    public ShowService(TvMazeClient tvMazeClient) {
        this.tvMazeClient = tvMazeClient;
    }

    /**
     * Busca shows en TVmaze y devuelve unicamente los atributos del contrato publico.
     *
     * @param searchQuery criterio de busqueda.
     * @return arreglo de shows; vacio si no hubo coincidencias.
     */
    public List<ShowSearchResponse> searchShows(String searchQuery) {
        List<ShowSearchResponse> shows = tvMazeClient.searchShows(searchQuery.trim()).stream()
                .map(TvMazeSearchResult::show)
                .filter(Objects::nonNull)
                .map(ShowMapper::toSearchResponse)
                .toList();
        log.info("Busqueda '{}' resolvio {} show(s).", searchQuery, shows.size());
        return shows;
    }

    /**
     * Devuelve el objeto show completo de TVmaze.
     *
     * @param showId identificador del show.
     */
    public TvMazeShow getShowById(long showId) {
        TvMazeShow show = tvMazeClient.findShowById(showId);
        log.info("Show {} recuperado: '{}'.", showId, show.name());
        return show;
    }
}
