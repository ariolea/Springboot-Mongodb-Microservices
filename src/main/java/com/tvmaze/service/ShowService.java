package com.tvmaze.service;

import com.tvmaze.client.TvMazeClient;
import com.tvmaze.client.dto.TvMazeSearchResult;
import com.tvmaze.client.dto.TvMazeShow;
import com.tvmaze.persistence.document.ShowDocument;
import com.tvmaze.persistence.repository.ShowCacheRepository;
import com.tvmaze.web.dto.CommentSummary;
import com.tvmaze.web.dto.ShowSearchResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Casos de uso del middleware: buscar shows por criterio y consultar un show por id.
 * La busqueda incluye los comentarios guardados en MongoDB para cada show.
 */
@Service
public class ShowService {

    private static final Logger log = LoggerFactory.getLogger(ShowService.class);

    private final TvMazeClient tvMazeClient;
    private final ShowCacheRepository showCacheRepository;
    private final CommentService commentService;

    public ShowService(TvMazeClient tvMazeClient,
                       ShowCacheRepository showCacheRepository,
                       CommentService commentService) {
        this.tvMazeClient = tvMazeClient;
        this.showCacheRepository = showCacheRepository;
        this.commentService = commentService;
    }

    /**
     * Busca shows en TVmaze, proyecta los atributos del contrato publico y agrega a cada
     * uno sus comentarios guardados.
     *
     * @param searchQuery criterio de busqueda.
     * @return arreglo de shows; vacio si no hubo coincidencias.
     */
    public List<ShowSearchResponse> searchShows(String searchQuery) {
        List<TvMazeShow> shows = tvMazeClient.searchShows(searchQuery.trim()).stream()
                .map(TvMazeSearchResult::show)
                .filter(Objects::nonNull)
                .toList();

        List<Long> showIds = shows.stream().map(TvMazeShow::id).filter(Objects::nonNull).toList();
        Map<Long, List<CommentSummary>> commentsByShow = commentService.findCommentsOf(showIds);

        List<ShowSearchResponse> response = shows.stream()
                .map(show -> ShowMapper.toSearchResponse(
                        show, commentsByShow.getOrDefault(show.id(), List.of())))
                .toList();

        log.info("Busqueda '{}' resolvio {} show(s).", searchQuery, response.size());
        return response;
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
