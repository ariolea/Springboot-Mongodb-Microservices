package com.tvmaze.web.dto;

import com.tvmaze.persistence.document.CommentDocument;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Comentario tal como se expone junto a un show: solo el texto y la calificacion.
 *
 * @param comment texto del comentario.
 * @param rating  calificacion de 0 a 5.
 */
@Schema(name = "Comment", description = "Comentario guardado para un show")
public record CommentSummary(

        @Schema(description = "Texto del comentario", example = "Excelente serie.")
        String comment,

        @Schema(description = "Calificacion de 0 a 5", example = "4")
        Integer rating) {

    /** Proyecta el documento de MongoDB al contrato publico. */
    public static CommentSummary from(CommentDocument document) {
        return new CommentSummary(document.comment(), document.rating());
    }
}
