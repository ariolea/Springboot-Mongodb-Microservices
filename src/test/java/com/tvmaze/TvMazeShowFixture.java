package com.tvmaze;

import com.tvmaze.client.dto.TvMazeCountry;
import com.tvmaze.client.dto.TvMazeExternals;
import com.tvmaze.client.dto.TvMazeImage;
import com.tvmaze.client.dto.TvMazeLink;
import com.tvmaze.client.dto.TvMazeLinks;
import com.tvmaze.client.dto.TvMazeNetwork;
import com.tvmaze.client.dto.TvMazeRating;
import com.tvmaze.client.dto.TvMazeSchedule;
import com.tvmaze.client.dto.TvMazeShow;
import java.util.List;

/**
 * Shows de ejemplo reutilizados por las pruebas.
 */
public final class TvMazeShowFixture {

    private TvMazeShowFixture() {
    }

    /** Show minimo con id y nombre, para pruebas que solo necesitan distinguirlos. */
    public static TvMazeShow withId(long id, String name) {
        return new TvMazeShow(id, null, name, null, null, List.of(), null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null);
    }

    /** Show completo equivalente a la respuesta real de /shows/1. */
    public static TvMazeShow underTheDome() {
        return new TvMazeShow(
                1L,
                "https://www.tvmaze.com/shows/1/under-the-dome",
                "Under the Dome",
                "Scripted",
                "English",
                List.of("Drama", "Science-Fiction", "Thriller"),
                "Ended",
                60,
                60,
                "2013-06-24",
                "2015-09-10",
                "http://www.cbs.com/shows/under-the-dome/",
                new TvMazeSchedule("22:00", List.of("Thursday")),
                new TvMazeRating(6.6),
                100,
                new TvMazeNetwork(2L, "CBS",
                        new TvMazeCountry("United States", "US", "America/New_York"),
                        "https://www.cbs.com/"),
                null,
                null,
                new TvMazeExternals(25988L, 264492L, "tt1553656"),
                new TvMazeImage("https://static.tvmaze.com/medium.jpg", "https://static.tvmaze.com/original.jpg"),
                "<p>Un pueblo aislado por una cupula.</p>",
                1789071819L,
                new TvMazeLinks(new TvMazeLink("https://api.tvmaze.com/shows/1", null), null, null));
    }
}
