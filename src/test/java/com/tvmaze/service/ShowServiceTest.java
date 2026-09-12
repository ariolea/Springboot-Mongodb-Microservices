package com.tvmaze.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tvmaze.TvMazeShowFixture;
import com.tvmaze.client.TvMazeClient;
import com.tvmaze.client.dto.TvMazeSearchResult;
import com.tvmaze.client.dto.TvMazeShow;
import com.tvmaze.exception.ShowNotFoundException;
import com.tvmaze.web.dto.ShowSearchResponse;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Casos de uso del middleware, aislados de la red.
 */
@ExtendWith(MockitoExtension.class)
class ShowServiceTest {

    @Mock
    private TvMazeClient tvMazeClient;

    @InjectMocks
    private ShowService showService;

    @Test
    @DisplayName("La busqueda proyecta unicamente los atributos del contrato publico")
    void searchProjectsContractAttributes() {
        when(tvMazeClient.searchShows("dome"))
                .thenReturn(List.of(new TvMazeSearchResult(0.9, TvMazeShowFixture.underTheDome())));

        List<ShowSearchResponse> results = showService.searchShows("dome");

        assertThat(results).hasSize(1);
        ShowSearchResponse show = results.get(0);
        assertThat(show.id()).isEqualTo(1L);
        assertThat(show.name()).isEqualTo("Under the Dome");
        assertThat(show.channel()).isEqualTo("CBS");
        assertThat(show.summary()).isEqualTo("<p>Un pueblo aislado por una cupula.</p>");
        assertThat(show.genres()).containsExactly("Drama", "Science-Fiction", "Thriller");
    }

    @Test
    @DisplayName("El criterio se normaliza antes de viajar a TVmaze")
    void searchTrimsCriteria() {
        when(tvMazeClient.searchShows("dome")).thenReturn(List.of());

        showService.searchShows("  dome  ");

        verify(tvMazeClient).searchShows("dome");
    }

    @Test
    @DisplayName("Los resultados sin show se descartan en lugar de romper la respuesta")
    void searchSkipsResultsWithoutShow() {
        when(tvMazeClient.searchShows(anyString()))
                .thenReturn(Arrays.asList(
                        new TvMazeSearchResult(0.9, TvMazeShowFixture.underTheDome()),
                        new TvMazeSearchResult(0.1, null)));

        assertThat(showService.searchShows("dome")).hasSize(1);
    }

    @Test
    @DisplayName("La consulta por id devuelve el show completo sin transformarlo")
    void getShowByIdReturnsCompleteShow() {
        TvMazeShow expected = TvMazeShowFixture.underTheDome();
        when(tvMazeClient.findShowById(1L)).thenReturn(expected);

        assertThat(showService.getShowById(1L)).isSameAs(expected);
    }

    @Test
    @DisplayName("Un show inexistente propaga ShowNotFoundException")
    void getShowByIdPropagatesNotFound() {
        when(tvMazeClient.findShowById(999999L)).thenThrow(new ShowNotFoundException(999999L));

        assertThatThrownBy(() -> showService.getShowById(999999L))
                .isInstanceOf(ShowNotFoundException.class);
    }
}
