package com.tvmaze.web;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tvmaze.TvMazeShowFixture;
import com.tvmaze.exception.ShowNotFoundException;
import com.tvmaze.exception.TvMazeRateLimitException;
import com.tvmaze.exception.TvMazeUnavailableException;
import com.tvmaze.service.ShowService;
import com.tvmaze.web.dto.CommentSummary;
import com.tvmaze.web.dto.ShowDetailResponse;
import com.tvmaze.web.dto.ShowSearchResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Contrato HTTP expuesto por el middleware.
 */
@WebMvcTest(ShowController.class)
class ShowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShowService showService;

    @Test
    @DisplayName("GET /api/v1/shows/search devuelve el arreglo de shows proyectado")
    void searchReturnsProjectedShows() throws Exception {
        when(showService.searchShows("dome")).thenReturn(List.of(
                new ShowSearchResponse(1L, "Under the Dome", "CBS", "<p>Sinopsis.</p>",
                        List.of("Drama", "Thriller"),
                        List.of(new CommentSummary("Muy buena.", 5), new CommentSummary("Regular.", 3)))));

        mockMvc.perform(get("/api/v1/shows/search").param("q", "dome"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Under the Dome"))
                .andExpect(jsonPath("$[0].channel").value("CBS"))
                .andExpect(jsonPath("$[0].summary").value("<p>Sinopsis.</p>"))
                .andExpect(jsonPath("$[0].genres[0]").value("Drama"))
                .andExpect(jsonPath("$[0].genres[1]").value("Thriller"))
                .andExpect(jsonPath("$[0].comments.length()").value(2))
                .andExpect(jsonPath("$[0].comments[0].comment").value("Muy buena."))
                .andExpect(jsonPath("$[0].comments[0].rating").value(5))
                .andExpect(jsonPath("$[0].comments[1].rating").value(3));
    }

    @Test
    @DisplayName("El criterio tambien se acepta en el parametro search_query")
    void searchAcceptsSearchQueryAlias() throws Exception {
        when(showService.searchShows("dome")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/shows/search").param("search_query", "dome"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(showService).searchShows("dome");
    }

    @Test
    @DisplayName("Una busqueda sin coincidencias devuelve un arreglo vacio, no un error")
    void searchWithoutMatchesReturnsEmptyArray() throws Exception {
        when(showService.searchShows(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/shows/search").param("q", "zzzzzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Un criterio ausente responde 400 sin llamar a TVmaze")
    void searchWithoutCriteriaReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/shows/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/shows/search"));

        verifyNoInteractions(showService);
    }

    @Test
    @DisplayName("Un criterio en blanco responde 400 sin llamar a TVmaze")
    void searchWithBlankCriteriaReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/shows/search").param("q", "   "))
                .andExpect(status().isBadRequest());

        verify(showService, never()).searchShows(anyString());
    }

    @Test
    @DisplayName("Un id no positivo responde 400 sin llamar a TVmaze")
    void getShowByIdRejectsNonPositiveId() throws Exception {
        mockMvc.perform(get("/api/v1/shows/-1"))
                .andExpect(status().isBadRequest());

        verify(showService, never()).getShowById(anyLong());
    }

    @Test
    @DisplayName("Un id no numerico responde 400")
    void getShowByIdRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/api/v1/shows/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/shows/{id} devuelve el objeto show completo")
    void getShowByIdReturnsCompleteShow() throws Exception {
        when(showService.getShowById(1L)).thenReturn(new ShowDetailResponse(
                TvMazeShowFixture.underTheDome(), List.of(new CommentSummary("Muy buena.", 5))));

        mockMvc.perform(get("/api/v1/shows/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Under the Dome"))
                .andExpect(jsonPath("$.type").value("Scripted"))
                .andExpect(jsonPath("$.language").value("English"))
                .andExpect(jsonPath("$.status").value("Ended"))
                .andExpect(jsonPath("$.schedule.days[0]").value("Thursday"))
                .andExpect(jsonPath("$.rating.average").value(6.6))
                .andExpect(jsonPath("$.network.name").value("CBS"))
                .andExpect(jsonPath("$.network.country.code").value("US"))
                .andExpect(jsonPath("$.externals.imdb").value("tt1553656"))
                .andExpect(jsonPath("$.image.medium").value("https://static.tvmaze.com/medium.jpg"))
                .andExpect(jsonPath("$._links.self.href").value("https://api.tvmaze.com/shows/1"))
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.comments[0].comment").value("Muy buena."))
                .andExpect(jsonPath("$.comments[0].rating").value(5));
    }

    @Test
    @DisplayName("Un show inexistente responde 404 con el cuerpo de error estandar")
    void getShowByIdReturnsNotFound() throws Exception {
        when(showService.getShowById(999999L)).thenThrow(new ShowNotFoundException(999999L));

        mockMvc.perform(get("/api/v1/shows/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No existe un show con id 999999 en TVmaze."));
    }

    @Test
    @DisplayName("Una caida de TVmaze se reporta como 502 Bad Gateway")
    void upstreamFailureReturnsBadGateway() throws Exception {
        when(showService.getShowById(1L))
                .thenThrow(new TvMazeUnavailableException("TVmaze respondio con estado 503."));

        mockMvc.perform(get("/api/v1/shows/1"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("TVmaze respondio con estado 503."));
    }

    @Test
    @DisplayName("El rate limit de TVmaze se reporta como 429")
    void rateLimitReturnsTooManyRequests() throws Exception {
        when(showService.searchShows("dome"))
                .thenThrow(new TvMazeRateLimitException("Limite de solicitudes excedido."));

        mockMvc.perform(get("/api/v1/shows/search").param("q", "dome"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }
}
