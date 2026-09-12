package com.tvmaze.web.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Proyeccion de un show devuelta por el endpoint de busqueda.
 *
 * @param id       identificador del show en TVmaze.
 * @param name     nombre del show.
 * @param channel  nombre de la cadena ({@code network}) o, si no aplica, de la
 *                 plataforma de streaming ({@code webChannel}).
 * @param summary  sinopsis del show, tal como la publica TVmaze (incluye etiquetas HTML).
 * @param genres   generos asociados al show; lista vacia si TVmaze no reporta ninguno.
 * @param comments comentarios guardados en MongoDB para ese show; lista vacia si no hay.
 */
@Schema(name = "ShowSearchResult", description = "Show encontrado a partir del criterio de busqueda")
public record ShowSearchResponse(

        @Schema(description = "Identificador del show en TVmaze", example = "139")
        Long id,

        @Schema(description = "Nombre del show", example = "Girls")
        String name,

        @Schema(description = "Cadena de TV o plataforma de streaming que transmite el show", example = "HBO")
        String channel,

        @Schema(description = "Sinopsis del show", example = "<p>This Emmy winning series...</p>")
        String summary,

        @Schema(description = "Generos del show", example = "[\"Drama\",\"Romance\"]")
        List<String> genres,

        @ArraySchema(schema = @Schema(implementation = CommentSummary.class),
                arraySchema = @Schema(description = "Comentarios guardados para el show"))
        List<CommentSummary> comments) {
}
