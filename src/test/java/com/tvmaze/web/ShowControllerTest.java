package com.tvmaze.web;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tvmaze.exception.TvMazeRateLimitException;
import com.tvmaze.service.ShowService;
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
                        List.of("Drama", "Thriller"))));

        mockMvc.perform(get("/api/v1/shows/search").param("q", "dome"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Under the Dome"))
                .andExpect(jsonPath("$[0].channel").value("CBS"))
                .andExpect(jsonPath("$[0].summary").value("<p>Sinopsis.</p>"))
                .andExpect(jsonPath("$[0].genres[0]").value("Drama"))
                .andExpect(jsonPath("$[0].genres[1]").value("Thriller"));
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
    @DisplayName("El rate limit de TVmaze se reporta como 429")
    void rateLimitReturnsTooManyRequests() throws Exception {
        when(showService.searchShows("dome"))
                .thenThrow(new TvMazeRateLimitException("Limite de solicitudes excedido."));

        mockMvc.perform(get("/api/v1/shows/search").param("q", "dome"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }
}
