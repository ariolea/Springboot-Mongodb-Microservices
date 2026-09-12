package com.tvmaze.web;

import com.tvmaze.web.dto.ShowDetailResponse;
import com.tvmaze.exception.InvalidSearchQueryException;
import com.tvmaze.service.ShowService;
import com.tvmaze.web.dto.ApiError;
import com.tvmaze.web.dto.ShowSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints publicos del middleware de TVmaze.
 */
@RestController
@RequestMapping(path = "/api/v1/shows", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Shows", description = "Busqueda y consulta de shows a traves de TVmaze")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    /**
     * Busca shows por criterio y devuelve un arreglo con id, nombre, canal, sinopsis y generos.
     * El criterio se acepta tanto en el parametro q como en search_query.
     */
    @GetMapping("/search")
    @Operation(summary = "Buscar shows por criterio",
            description = "Consulta GET /search/shows?q={query} en TVmaze y devuelve un arreglo de shows "
                    + "con los comentarios guardados para cada uno. "
                    + "Atributos: id, name, channel, summary, genres y comments. "
                    + "El criterio puede enviarse en el parametro q o en search_query.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arreglo de shows encontrados",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = ShowSearchResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Criterio de busqueda ausente o vacio",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "429", description = "Limite de peticiones de TVmaze excedido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "502", description = "TVmaze no esta disponible",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<ShowSearchResponse> searchShows(
            @Parameter(description = "Criterio de busqueda", example = "girls")
            @RequestParam(name = "q", required = false) String q,
            @Parameter(description = "Alias del criterio de busqueda", example = "girls")
            @RequestParam(name = "search_query", required = false) String searchQuery) {

        String criteria = StringUtils.hasText(q) ? q : searchQuery;
        if (!StringUtils.hasText(criteria)) {
            throw new InvalidSearchQueryException(
                    "El criterio de busqueda es obligatorio. Envielo en el parametro q o search_query.");
        }
        return showService.searchShows(criteria);
    }

    /**
     * Devuelve el objeto show completo a partir de su id.
     */
    @GetMapping("/{showId}")
    @Operation(summary = "Obtener un show por id",
            description = "Devuelve el objeto show completo con un arreglo comments. Antes de consumir el API "
                    + "valida la cache en MongoDB: si el id ya esta registrado responde desde ahi; si no, "
                    + "consulta GET /shows/{show_id} en TVmaze y guarda el resultado antes de responder.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Show encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ShowDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "El id no es un entero positivo",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "El show no existe en TVmaze",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "429", description = "Limite de peticiones de TVmaze excedido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "502", description = "TVmaze no esta disponible",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ShowDetailResponse getShowById(
            @Parameter(description = "Identificador del show en TVmaze", example = "1")
            @PathVariable @Positive(message = "El id del show debe ser un entero positivo.") long showId) {
        return showService.getShowById(showId);
    }
}
