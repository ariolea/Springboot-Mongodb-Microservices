package com.tvmaze.web;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tvmaze.persistence.document.CommentDocument;
import com.tvmaze.service.CommentService;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Contrato HTTP del endpoint de comentarios.
 */
@WebMvcTest(CommentController.class)
class CommentControllerTest {

    private static final String URL = "/api/v1/comments";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("POST /api/v1/comments registra el comentario y devuelve 201 con el estado")
    void createCommentReturnsCreated() throws Exception {
        when(commentService.saveComment(1L, "Excelente serie.", 4)).thenReturn(
                new CommentDocument("6706f1c0a2e4b0a1c2d3e4f5", 1L, "Excelente serie.", 4,
                        Instant.parse("2026-09-12T10:15:30Z")));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"show_id": 1, "comment": "Excelente serie.", "rating": 4}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Comentario registrado para el show 1."))
                .andExpect(jsonPath("$.comment_id").value("6706f1c0a2e4b0a1c2d3e4f5"))
                .andExpect(jsonPath("$.show_id").value(1))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.created_at").exists());
    }

    @Test
    @DisplayName("El id del show tambien se acepta como showId")
    void createCommentAcceptsCamelCaseAlias() throws Exception {
        when(commentService.saveComment(anyLong(), anyString(), anyInt())).thenReturn(
                new CommentDocument("id", 2993L, "Buena.", 5, Instant.now()));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"showId": 2993, "comment": "Buena.", "rating": 5}
                        """))
                .andExpect(status().isCreated());

        verify(commentService).saveComment(eq(2993L), eq("Buena."), eq(5));
    }

    @Test
    @DisplayName("Una calificacion fuera del rango 0-5 responde 400 sin tocar la base")
    void createCommentRejectsRatingOutOfRange() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"show_id": 1, "comment": "Excelente serie.", "rating": 6}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El campo rating debe ser menor o igual a 5."));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"show_id": 1, "comment": "Excelente serie.", "rating": -1}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El campo rating debe ser mayor o igual a 0."));

        verify(commentService, never()).saveComment(anyLong(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Un comentario vacio o un show_id ausente responden 400")
    void createCommentRejectsMissingFields() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"show_id": 1, "comment": "   ", "rating": 3}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El campo comment es obligatorio."));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"comment": "Buena.", "rating": 3}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El campo show_id es obligatorio."));

        verify(commentService, never()).saveComment(anyLong(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Un JSON malformado responde 400")
    void createCommentRejectsMalformedJson() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"show_id\": 1,"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("El cuerpo de la peticion no es un JSON valido o no se pudo interpretar."));
    }

    @Test
    @DisplayName("Si MongoDB no responde, la peticion se reporta como 503")
    void createCommentReportsDatabaseFailure() throws Exception {
        when(commentService.saveComment(anyLong(), anyString(), anyInt()))
                .thenThrow(new DataAccessResourceFailureException("sin conexion"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"show_id": 1, "comment": "Excelente serie.", "rating": 4}
                        """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}
