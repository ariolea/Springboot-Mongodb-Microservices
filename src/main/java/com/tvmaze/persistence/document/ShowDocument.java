package com.tvmaze.persistence.document;

import com.tvmaze.client.dto.TvMazeShow;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Cache persistente de un show en MongoDB: el documento se guarda la primera vez que
 * se consulta a TVmaze y a partir de entonces se responde desde aqui.
 *
 * @param id       identificador del show en TVmaze; es tambien el {@code _id} del documento.
 * @param show     objeto show completo tal como lo devolvio TVmaze.
 * @param cachedAt momento en que se guardo el documento.
 */
@Document(collection = ShowDocument.COLLECTION)
public record ShowDocument(

        @Id Long id,

        TvMazeShow show,

        Instant cachedAt) {

    /** Nombre de la coleccion en MongoDB. */
    public static final String COLLECTION = "shows";

    /** Crea el documento a partir de la respuesta de TVmaze. */
    public static ShowDocument of(TvMazeShow show) {
        return new ShowDocument(show.id(), show, Instant.now());
    }
}
