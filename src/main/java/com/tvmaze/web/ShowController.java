package com.tvmaze.web;

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
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

}
