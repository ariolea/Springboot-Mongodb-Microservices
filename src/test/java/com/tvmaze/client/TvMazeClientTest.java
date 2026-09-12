package com.tvmaze.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.tvmaze.client.dto.TvMazeSearchResult;
import com.tvmaze.client.dto.TvMazeShow;
import com.tvmaze.exception.TvMazeUnavailableException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Verifica el contrato con TVmaze: rutas invocadas, deserializacion y traduccion de errores.
 */
class TvMazeClientTest {

    private static final String BASE_URL = "https://api.tvmaze.test";

    private MockRestServiceServer server;
    private TvMazeClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new TvMazeClient(builder.build());
    }

    @Test
    @DisplayName("La busqueda invoca /search/shows y deserializa el arreglo de resultados")
    void searchShowsDeserializesResults() {
        String payload = """
                [
                  {
                    "score": 0.89,
                    "show": {
                      "id": 139,
                      "name": "Girls",
                      "genres": ["Drama", "Romance"],
                      "network": {"id": 8, "name": "HBO", "country": {"name": "United States", "code": "US", "timezone": "America/New_York"}},
                      "webChannel": null,
                      "summary": "<p>Comedia sobre veinteaneras.</p>",
                      "_links": {"self": {"href": "https://api.tvmaze.com/shows/139"}}
                    }
                  }
                ]
                """;
        server.expect(requestTo(BASE_URL + "/search/shows?q=girls"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        List<TvMazeSearchResult> results = client.searchShows("girls");

        server.verify();
        assertThat(results).hasSize(1);
        TvMazeShow show = results.get(0).show();
        assertThat(show.id()).isEqualTo(139L);
        assertThat(show.name()).isEqualTo("Girls");
        assertThat(show.genres()).containsExactly("Drama", "Romance");
        assertThat(show.network().name()).isEqualTo("HBO");
        assertThat(show.links().self().href()).isEqualTo("https://api.tvmaze.com/shows/139");
    }

    @Test
    @DisplayName("Una busqueda sin coincidencias devuelve una lista vacia")
    void searchShowsWithoutMatchesReturnsEmptyList() {
        server.expect(requestTo(BASE_URL + "/search/shows?q=zzzzzz"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThat(client.searchShows("zzzzzz")).isEmpty();
        server.verify();
    }

    @Test
    @DisplayName("El criterio se codifica correctamente en el query string")
    void searchShowsEncodesQuery() {
        server.expect(requestTo(BASE_URL + "/search/shows?q=breaking%20bad"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        client.searchShows("breaking bad");

        server.verify();
    }

    @Test
    @DisplayName("Un 5xx de TVmaze se traduce a TvMazeUnavailableException")
    void searchShowsTranslatesServerError() {
        server.expect(requestTo(BASE_URL + "/search/shows?q=girls"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.searchShows("girls"))
                .isInstanceOf(TvMazeUnavailableException.class)
                .hasMessageContaining("503");
        server.verify();
    }
}
