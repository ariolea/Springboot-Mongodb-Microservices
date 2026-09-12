package com.tvmaze.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import com.tvmaze.persistence.document.CommentDocument;
import java.time.Instant;

/**
 * Estado de la peticion de registro de un comentario.
 *
 * @param status    codigo HTTP con que se resolvio la peticion.
 * @param message   descripcion legible del resultado.
 * @param commentId id que MongoDB asigno al comentario.
 * @param showId    show al que quedo ligado.
 * @param rating    calificacion registrada.
 * @param createdAt momento del registro.
 */
@Schema(name = "CommentResponse", description = "Estado del registro de un comentario")
public record CommentResponse(

        @Schema(description = "Codigo HTTP del resultado", example = "201")
        int status,

        @Schema(description = "Descripcion del resultado", example = "Comentario registrado para el show 1.")
        String message,

        @Schema(description = "Id asignado por MongoDB", example = "66f1c0a2e4b0a1c2d3e4f5a6")
        @JsonProperty("comment_id")
        String commentId,

        @Schema(description = "Id del show", example = "1")
        @JsonProperty("show_id")
        Long showId,

        @Schema(description = "Calificacion registrada", example = "4")
        Integer rating,

        @Schema(description = "Fecha de registro")
        @JsonProperty("created_at")
        Instant createdAt) {

    /** Construye la respuesta a partir del documento ya persistido. */
    public static CommentResponse created(CommentDocument document) {
        return new CommentResponse(
                201,
                "Comentario registrado para el show %d.".formatted(document.showId()),
                document.id(),
                document.showId(),
                document.rating(),
                document.createdAt());
    }
}
