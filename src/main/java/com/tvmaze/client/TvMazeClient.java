package com.tvmaze.client;

import com.tvmaze.client.dto.TvMazeSearchResult;
import com.tvmaze.config.CacheConfig;
import com.tvmaze.client.dto.TvMazeShow;
import com.tvmaze.exception.ShowNotFoundException;
import com.tvmaze.exception.TvMazeRateLimitException;
import com.tvmaze.exception.TvMazeUnavailableException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Unico punto de contacto con el API publico de TVmaze.
 *
 * <p>Traduce los modos de falla del proveedor (404, 429, 5xx, timeouts) a excepciones
 * propias del dominio, de modo que las capas superiores no dependan de detalles HTTP.
 */
@Component
public class TvMazeClient {

    private static final Logger log = LoggerFactory.getLogger(TvMazeClient.class);

    private static final ParameterizedTypeReference<List<TvMazeSearchResult>> SEARCH_RESULTS =
            new ParameterizedTypeReference<>() {
            };

    private static final int MAX_LOGGED_BODY_LENGTH = 300;

    private final RestClient restClient;

    public TvMazeClient(RestClient tvMazeRestClient) {
        this.restClient = tvMazeRestClient;
    }

    /**
     * Busca shows por criterio: {@code GET /search/shows?q={query}}.
     *
     * @param query criterio de busqueda, ya validado como no vacio.
     * @return resultados ordenados por relevancia; lista vacia si no hay coincidencias.
     *         El resultado se cachea en memoria: lo que cambia seguido son los comentarios,
     *         que se resuelven fuera de esta cache.
     */
    @Cacheable(cacheNames = CacheConfig.SHOW_SEARCH_CACHE, key = "#query.toLowerCase()")
    public List<TvMazeSearchResult> searchShows(String query) {
        log.debug("Consultando TVmaze: /search/shows?q={}", query);
        try {
            List<TvMazeSearchResult> results = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search/shows").queryParam("q", query).build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> handleUpstreamError(response))
                    .body(SEARCH_RESULTS);
            return results != null ? results : List.of();
        } catch (ResourceAccessException ex) {
            throw new TvMazeUnavailableException(
                    "No fue posible contactar a TVmaze para la busqueda solicitada.", ex);
        } catch (RestClientException ex) {
            throw new TvMazeUnavailableException("Respuesta invalida de TVmaze en la busqueda de shows.", ex);
        }
    }

    /**
     * Obtiene el show completo por id: {@code GET /shows/{show_id}}.
     *
     * @param showId identificador del show en TVmaze.
     * @return el objeto show completo.
     * @throws ShowNotFoundException si TVmaze no conoce ese id.
     */
    public TvMazeShow findShowById(long showId) {
        log.debug("Consultando TVmaze: /shows/{}", showId);
        try {
            TvMazeShow show = restClient.get()
                    .uri("/shows/{showId}", showId)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(),
                            (request, response) -> {
                                throw new ShowNotFoundException(showId);
                            })
                    .onStatus(HttpStatusCode::isError, (request, response) -> handleUpstreamError(response))
                    .body(TvMazeShow.class);

            if (show == null) {
                throw new TvMazeUnavailableException(
                        "TVmaze devolvio una respuesta vacia para el show %d.".formatted(showId));
            }
            return show;
        } catch (ResourceAccessException ex) {
            throw new TvMazeUnavailableException(
                    "No fue posible contactar a TVmaze para el show %d.".formatted(showId), ex);
        } catch (RestClientException ex) {
            throw new TvMazeUnavailableException(
                    "Respuesta invalida de TVmaze para el show %d.".formatted(showId), ex);
        }
    }

    private void handleUpstreamError(ClientHttpResponse response) throws IOException {
        HttpStatusCode status = response.getStatusCode();
        String body = readBodySafely(response);
        log.warn("TVmaze respondio con estado {}. Cuerpo: {}", status, body);

        if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            throw new TvMazeRateLimitException(
                    "TVmaze rechazo la peticion por limite de solicitudes. Intente nuevamente en unos segundos.");
        }
        throw new TvMazeUnavailableException("TVmaze respondio con estado %d.".formatted(status.value()));
    }

    private String readBodySafely(ClientHttpResponse response) {
        try (var body = response.getBody()) {
            String content = new String(body.readAllBytes(), StandardCharsets.UTF_8);
            return content.length() > MAX_LOGGED_BODY_LENGTH
                    ? content.substring(0, MAX_LOGGED_BODY_LENGTH) + "..."
                    : content;
        } catch (IOException ex) {
            return "<sin cuerpo legible>";
        }
    }
}
