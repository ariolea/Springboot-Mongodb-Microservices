package com.tvmaze.web;

import com.tvmaze.persistence.document.CommentDocument;
import com.tvmaze.service.CommentService;
import com.tvmaze.web.dto.ApiError;
import com.tvmaze.web.dto.CommentRequest;
import com.tvmaze.web.dto.CommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registro de calificaciones y comentarios de un show en MongoDB.
 */
@RestController
@RequestMapping(path = "/api/v1/comments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Comments", description = "Calificaciones y comentarios ligados a un show")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Guarda una calificacion y un comentario ligados al id del show y devuelve
     * el estado de la peticion.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registrar una calificacion y un comentario",
            description = "Guarda en la coleccion 'comments' de MongoDB un comentario ligado al id del show. "
                    + "Recibe show_id, comment y rating (0-5), y devuelve el estado de la peticion.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comentario registrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos: falta show_id o comment, "
                    + "o rating fuera del rango 0-5",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "No fue posible guardar en MongoDB",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest request) {
        CommentDocument saved = commentService.saveComment(
                request.showId(), request.comment(), request.rating());
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.created(saved));
    }
}
