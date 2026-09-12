package com.tvmaze.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tvmaze.client.dto.TvMazeNetwork;
import com.tvmaze.client.dto.TvMazeShow;
import com.tvmaze.client.dto.TvMazeWebChannel;
import com.tvmaze.web.dto.ShowSearchResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Reglas de traduccion entre el modelo de TVmaze y el contrato del middleware.
 */
class ShowMapperTest {

    @Test
    @DisplayName("El canal toma el nombre de la cadena cuando el show es de TV abierta o de paga")
    void channelPrefersNetwork() {
        TvMazeShow show = show(
                new TvMazeNetwork(8L, "HBO", null, null),
                new TvMazeWebChannel(1L, "HBO Max", null, null),
                List.of("Drama"));

        ShowSearchResponse response = ShowMapper.toSearchResponse(show);

        assertThat(response.channel()).isEqualTo("HBO");
        assertThat(response.id()).isEqualTo(139L);
        assertThat(response.name()).isEqualTo("Girls");
        assertThat(response.summary()).isEqualTo("<p>Sinopsis.</p>");
        assertThat(response.genres()).containsExactly("Drama");
    }

    @Test
    @DisplayName("El canal cae a la plataforma de streaming cuando no hay cadena")
    void channelFallsBackToWebChannel() {
        TvMazeShow show = show(null, new TvMazeWebChannel(1L, "Netflix", null, null), List.of("Drama"));

        assertThat(ShowMapper.toSearchResponse(show).channel()).isEqualTo("Netflix");
    }

    @Test
    @DisplayName("El canal es nulo cuando TVmaze no reporta cadena ni plataforma")
    void channelIsNullWhenNoSource() {
        TvMazeShow show = show(null, null, List.of("Drama"));

        assertThat(ShowMapper.toSearchResponse(show).channel()).isNull();
    }

    @Test
    @DisplayName("Una cadena sin nombre no opaca a la plataforma de streaming")
    void blankNetworkNameFallsBackToWebChannel() {
        TvMazeShow show = show(
                new TvMazeNetwork(8L, "  ", null, null),
                new TvMazeWebChannel(1L, "Disney+", null, null),
                List.of());

        assertThat(ShowMapper.toSearchResponse(show).channel()).isEqualTo("Disney+");
    }

    @Test
    @DisplayName("Los generos ausentes se exponen como arreglo vacio, nunca como nulo")
    void nullGenresBecomeEmptyList() {
        TvMazeShow show = show(new TvMazeNetwork(8L, "HBO", null, null), null, null);

        assertThat(ShowMapper.toSearchResponse(show).genres()).isEmpty();
    }

    private static TvMazeShow show(TvMazeNetwork network, TvMazeWebChannel webChannel, List<String> genres) {
        return new TvMazeShow(139L, null, "Girls", null, null, genres, null, null, null, null, null, null,
                null, null, null, network, webChannel, null, null, null, "<p>Sinopsis.</p>", null, null);
    }
}
