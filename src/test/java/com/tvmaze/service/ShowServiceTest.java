package com.tvmaze.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tvmaze.TvMazeShowFixture;
import com.tvmaze.client.TvMazeClient;
import com.tvmaze.client.dto.TvMazeSearchResult;
import com.tvmaze.client.dto.TvMazeShow;
import com.tvmaze.exception.ShowNotFoundException;
import com.tvmaze.persistence.document.ShowDocument;
import com.tvmaze.persistence.repository.ShowCacheRepository;
import com.tvmaze.web.dto.CommentSummary;
import com.tvmaze.web.dto.ShowDetailResponse;
import com.tvmaze.web.dto.ShowSearchResponse;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Casos de uso del middleware, aislados de la red y de MongoDB.
 */
@ExtendWith(MockitoExtension.class)
class ShowServiceTest {

    @Mock
    private TvMazeClient tvMazeClient;

    @Mock
    private ShowCacheRepository showCacheRepository;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private ShowService showService;

    @Test
    @DisplayName("La busqueda proyecta los atributos del contrato publico")
    void searchProjectsContractAttributes() {
        when(tvMazeClient.searchShows("dome"))
                .thenReturn(List.of(new TvMazeSearchResult(0.9, TvMazeShowFixture.underTheDome())));
        when(commentService.findCommentsOf(List.of(1L))).thenReturn(Map.of());

        List<ShowSearchResponse> results = showService.searchShows("dome");

        assertThat(results).hasSize(1);
        ShowSearchResponse show = results.get(0);
        assertThat(show.id()).isEqualTo(1L);
        assertThat(show.name()).isEqualTo("Under the Dome");
        assertThat(show.channel()).isEqualTo("CBS");
        assertThat(show.summary()).isEqualTo("<p>Un pueblo aislado por una cupula.</p>");
        assertThat(show.genres()).containsExactly("Drama", "Science-Fiction", "Thriller");
        assertThat(show.comments()).isEmpty();
    }

    @Test
    @DisplayName("Cada show del resultado recibe sus propios comentarios")
    void searchAttachesCommentsToEachShow() {
        TvMazeShow dome = TvMazeShowFixture.underTheDome();
        TvMazeShow other = TvMazeShowFixture.withId(2993L, "Stranger Things");
        when(tvMazeClient.searchShows("dome")).thenReturn(List.of(
                new TvMazeSearchResult(0.9, dome),
                new TvMazeSearchResult(0.4, other)));
        when(commentService.findCommentsOf(List.of(1L, 2993L))).thenReturn(Map.of(
                1L, List.of(new CommentSummary("Muy buena.", 5), new CommentSummary("Regular.", 3))));

        List<ShowSearchResponse> results = showService.searchShows("dome");

        assertThat(results.get(0).comments())
                .extracting(CommentSummary::comment)
                .containsExactly("Muy buena.", "Regular.");
        assertThat(results.get(1).comments()).isEmpty();
    }

    @Test
    @DisplayName("Los comentarios se piden en una sola consulta para todos los shows")
    void searchResolvesCommentsInASingleQuery() {
        when(tvMazeClient.searchShows(anyString())).thenReturn(List.of(
                new TvMazeSearchResult(0.9, TvMazeShowFixture.underTheDome()),
                new TvMazeSearchResult(0.4, TvMazeShowFixture.withId(2993L, "Stranger Things"))));
        when(commentService.findCommentsOf(anyCollection())).thenReturn(Map.of());

        showService.searchShows("dome");

        verify(commentService).findCommentsOf(List.of(1L, 2993L));
        verify(commentService, never()).findCommentsOf(anyLong());
    }

    @Test
    @DisplayName("El criterio se normaliza antes de viajar a TVmaze")
    void searchTrimsCriteria() {
        when(tvMazeClient.searchShows("dome")).thenReturn(List.of());
        when(commentService.findCommentsOf(List.of())).thenReturn(Map.of());

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
        when(commentService.findCommentsOf(anyCollection())).thenReturn(Map.of());

        assertThat(showService.searchShows("dome")).hasSize(1);
    }

    @Test
    @DisplayName("Si el show ya esta en MongoDB se responde desde ahi y no se consume TVmaze")
    void getShowByIdServesFromMongoCache() {
        TvMazeShow cached = TvMazeShowFixture.underTheDome();
        when(showCacheRepository.findById(1L))
                .thenReturn(Optional.of(new ShowDocument(1L, cached, Instant.now())));
        when(commentService.findCommentsOf(1L)).thenReturn(List.of());

        assertThat(showService.getShowById(1L).show()).isSameAs(cached);

        verifyNoInteractions(tvMazeClient);
        verify(showCacheRepository, never()).save(any());
    }

    @Test
    @DisplayName("Si el show no esta en MongoDB se consulta TVmaze y se guarda antes de responder")
    void getShowByIdFallsBackToTvMazeAndPersists() {
        TvMazeShow show = TvMazeShowFixture.underTheDome();
        when(showCacheRepository.findById(1L)).thenReturn(Optional.empty());
        when(tvMazeClient.findShowById(1L)).thenReturn(show);
        when(commentService.findCommentsOf(1L)).thenReturn(List.of());

        assertThat(showService.getShowById(1L).show()).isSameAs(show);

        ArgumentCaptor<ShowDocument> captor = ArgumentCaptor.forClass(ShowDocument.class);
        verify(showCacheRepository).save(captor.capture());
        ShowDocument saved = captor.getValue();
        assertThat(saved.id()).isEqualTo(1L);
        assertThat(saved.show()).isSameAs(show);
        assertThat(saved.cachedAt()).isNotNull();
    }

    @Test
    @DisplayName("El show por id incluye sus comentarios")
    void getShowByIdAttachesComments() {
        when(showCacheRepository.findById(1L)).thenReturn(
                Optional.of(new ShowDocument(1L, TvMazeShowFixture.underTheDome(), Instant.now())));
        when(commentService.findCommentsOf(1L))
                .thenReturn(List.of(new CommentSummary("Muy buena.", 5)));

        ShowDetailResponse response = showService.getShowById(1L);

        assertThat(response.comments()).containsExactly(new CommentSummary("Muy buena.", 5));
    }

    @Test
    @DisplayName("Un show inexistente propaga ShowNotFoundException y no se guarda nada")
    void getShowByIdPropagatesNotFound() {
        when(showCacheRepository.findById(999999L)).thenReturn(Optional.empty());
        when(tvMazeClient.findShowById(999999L)).thenThrow(new ShowNotFoundException(999999L));

        assertThatThrownBy(() -> showService.getShowById(999999L))
                .isInstanceOf(ShowNotFoundException.class);

        verify(showCacheRepository, never()).save(any());
        verifyNoInteractions(commentService);
    }
}
