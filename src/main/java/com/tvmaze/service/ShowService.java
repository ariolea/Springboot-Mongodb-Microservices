package com.tvmaze.service;

import com.tvmaze.client.TvMazeClient;
import com.tvmaze.client.dto.TvMazeSearchResult;
import com.tvmaze.client.dto.TvMazeShow;
import com.tvmaze.persistence.document.ShowDocument;
import com.tvmaze.persistence.repository.ShowCacheRepository;
import com.tvmaze.web.dto.ShowSearchResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private final ShowCacheRepository showCacheRepository;

    public ShowService(TvMazeClient tvMazeClient, ShowCacheRepository showCacheRepository) {
        this.tvMazeClient = tvMazeClient;
        this.showCacheRepository = showCacheRepository;
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
     * Devuelve el objeto show completo, usando MongoDB como cache del API de TVmaze:
     * si el id ya esta registrado se responde desde la base y no se consume el API;
     * si no, se consulta TVmaze y el resultado se guarda antes de responder.
     *
     * @param showId identificador del show.
     */
    public TvMazeShow getShowById(long showId) {
        Optional<ShowDocument> cached = showCacheRepository.findById(showId);
        if (cached.isPresent()) {
            log.info("Show {} servido desde la cache de MongoDB.", showId);
            return cached.get().show();
        }

        TvMazeShow show = tvMazeClient.findShowById(showId);
        showCacheRepository.save(ShowDocument.of(show));
        log.info("Show {} recuperado de TVmaze y guardado en MongoDB: '{}'.", showId, show.name());
        return show;
    }
}
