package com.tvmaze.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tvmaze.persistence.document.CommentDocument;
import com.tvmaze.persistence.repository.CommentRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Registro de comentarios, aislado de MongoDB.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("El comentario se guarda ligado al id del show, con fecha y sin espacios sobrantes")
    void saveCommentPersistsDocument() {
        when(commentRepository.save(any(CommentDocument.class)))
                .thenAnswer(invocation -> {
                    CommentDocument argument = invocation.getArgument(0);
                    return new CommentDocument("6706f1c0a2e4b0a1c2d3e4f5", argument.showId(),
                            argument.comment(), argument.rating(), argument.createdAt());
                });

        CommentDocument saved = commentService.saveComment(1L, "  Excelente serie.  ", 4);

        ArgumentCaptor<CommentDocument> captor = ArgumentCaptor.forClass(CommentDocument.class);
        verify(commentRepository).save(captor.capture());
        CommentDocument sent = captor.getValue();
        assertThat(sent.id()).isNull();
        assertThat(sent.showId()).isEqualTo(1L);
        assertThat(sent.comment()).isEqualTo("Excelente serie.");
        assertThat(sent.rating()).isEqualTo(4);
        assertThat(sent.createdAt()).isNotNull().isBeforeOrEqualTo(Instant.now());

        assertThat(saved.id()).isEqualTo("6706f1c0a2e4b0a1c2d3e4f5");
    }

    @Test
    @DisplayName("Una calificacion en los limites del rango se guarda tal cual")
    void saveCommentAcceptsRangeBoundaries() {
        when(commentRepository.save(any(CommentDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(commentService.saveComment(1L, "Sin comentarios.", 0).rating()).isZero();
        assertThat(commentService.saveComment(1L, "Imperdible.", 5).rating()).isEqualTo(5);
    }
}
