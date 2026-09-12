package com.tvmaze.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.tvmaze.client.dto.TvMazeSearchResult;
import com.tvmaze.client.dto.TvMazeShow;
import com.tvmaze.exception.ShowNotFoundException;
import com.tvmaze.exception.TvMazeRateLimitException;
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
    @DisplayName("La consulta por id deserializa el show completo")
    void findShowByIdDeserializesShow() {
        String payload = """
                {
                  "id": 1,
                  "url": "https://www.tvmaze.com/shows/1/under-the-dome",
                  "name": "Under the Dome",
                  "type": "Scripted",
                  "language": "English",
                  "genres": ["Drama", "Science-Fiction", "Thriller"],
                  "status": "Ended",
                  "runtime": 60,
                  "averageRuntime": 60,
                  "premiered": "2013-06-24",
                  "ended": "2015-09-10",
                  "schedule": {"time": "22:00", "days": ["Thursday"]},
                  "rating": {"average": 6.6},
                  "weight": 100,
                  "network": {"id": 2, "name": "CBS", "country": {"name": "United States", "code": "US", "timezone": "America/New_York"}},
                  "externals": {"tvrage": 25988, "thetvdb": 264492, "imdb": "tt1553656"},
                  "image": {"medium": "https://static.tvmaze.com/medium.jpg", "original": "https://static.tvmaze.com/original.jpg"},
                  "summary": "<p>Un pueblo aislado por una cupula.</p>",
                  "updated": 1789071819,
                  "_links": {"self": {"href": "https://api.tvmaze.com/shows/1"}}
                }
                """;
        server.expect(requestTo(BASE_URL + "/shows/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        TvMazeShow show = client.findShowById(1L);

        server.verify();
        assertThat(show.name()).isEqualTo("Under the Dome");
        assertThat(show.schedule().days()).containsExactly("Thursday");
        assertThat(show.rating().average()).isEqualTo(6.6);
        assertThat(show.externals().imdb()).isEqualTo("tt1553656");
        assertThat(show.image().medium()).isEqualTo("https://static.tvmaze.com/medium.jpg");
    }

    @Test
    @DisplayName("Un 404 de TVmaze se traduce a ShowNotFoundException")
    void findShowByIdTranslatesNotFound() {
        server.expect(requestTo(BASE_URL + "/shows/999999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.findShowById(999999L))
                .isInstanceOf(ShowNotFoundException.class)
                .hasMessageContaining("999999");
        server.verify();
    }

    @Test
    @DisplayName("Un 429 de TVmaze se traduce a TvMazeRateLimitException")
    void findShowByIdTranslatesRateLimit() {
        server.expect(requestTo(BASE_URL + "/shows/1"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> client.findShowById(1L))
                .isInstanceOf(TvMazeRateLimitException.class);
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
