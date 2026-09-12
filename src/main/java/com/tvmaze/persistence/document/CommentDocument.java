package com.tvmaze.persistence.document;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Calificacion y comentario de un usuario, ligados al id del show.
 *
 * @param id        identificador del comentario, generado por MongoDB.
 * @param showId    id del show en TVmaze al que pertenece el comentario.
 * @param comment   texto del comentario.
 * @param rating    calificacion de 0 a 5.
 * @param createdAt momento en que se registro.
 */
@Document(collection = CommentDocument.COLLECTION)
public record CommentDocument(

        @Id String id,

        Long showId,

        String comment,

        Integer rating,

        Instant createdAt) {

    /** Nombre de la coleccion en MongoDB. */
    public static final String COLLECTION = "comments";

    /** Crea un comentario nuevo; el id lo asigna MongoDB al insertarlo. */
    public static CommentDocument of(Long showId, String comment, Integer rating) {
        return new CommentDocument(null, showId, comment, rating, Instant.now());
    }
}
