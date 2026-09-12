package com.tvmaze.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/**
 * Cuerpo uniforme de error del middleware.
 *
 * @param timestamp momento en que se genero el error.
 * @param status    codigo HTTP asociado.
 * @param error     nombre corto del estado HTTP.
 * @param message   descripcion legible de la causa.
 * @param path      ruta solicitada.
 */
@Schema(name = "ApiError", description = "Representacion estandar de un error")
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path) {
}
