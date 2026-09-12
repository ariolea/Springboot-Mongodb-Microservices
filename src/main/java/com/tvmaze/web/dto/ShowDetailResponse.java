package com.tvmaze.web.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.tvmaze.client.dto.TvMazeShow;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Objeto show completo de TVmaze con el arreglo de comentarios agregado.
 *
 * <p>{@code @JsonUnwrapped} hace que los campos del show se serialicen al mismo nivel
 * que {@code comments}, de modo que la respuesta sigue siendo el objeto show tal como
 * lo publica TVmaze, con un atributo extra.
 *
 * @param show     objeto show completo devuelto por TVmaze.
 * @param comments comentarios guardados en MongoDB para ese show; lista vacia si no hay.
 */
@Schema(name = "ShowDetail", description = "Objeto show completo con sus comentarios")
public record ShowDetailResponse(

        @JsonUnwrapped TvMazeShow show,

        @ArraySchema(schema = @Schema(implementation = CommentSummary.class),
                arraySchema = @Schema(description = "Comentarios guardados para el show"))
        List<CommentSummary> comments) {
}
