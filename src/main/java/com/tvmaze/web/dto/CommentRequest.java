package com.tvmaze.web.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Calificacion y comentario que el cliente envia para un show.
 *
 * @param showId  id del show en TVmaze al que se liga el comentario.
 * @param comment texto del comentario.
 * @param rating  calificacion entre 0 y 5.
 */
@Schema(name = "CommentRequest", description = "Comentario y calificacion de un show")
public record CommentRequest(

        @Schema(description = "Id del show en TVmaze", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("show_id")
        @JsonAlias("showId")
        @NotNull(message = "El campo show_id es obligatorio.")
        @Positive(message = "El campo show_id debe ser un entero positivo.")
        Long showId,

        @Schema(description = "Texto del comentario", example = "Excelente serie, el final se siente apresurado.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El campo comment es obligatorio.")
        @Size(max = 2000, message = "El campo comment no puede exceder 2000 caracteres.")
        String comment,

        @Schema(description = "Calificacion de 0 a 5", example = "4", minimum = "0", maximum = "5",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El campo rating es obligatorio.")
        @Min(value = 0, message = "El campo rating debe ser mayor o igual a 0.")
        @Max(value = 5, message = "El campo rating debe ser menor o igual a 5.")
        Integer rating) {
}
