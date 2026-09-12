package com.tvmaze.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tvmaze.persistence.document.CommentDocument;
import com.tvmaze.persistence.repository.CommentRepository;
import com.tvmaze.web.dto.CommentSummary;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
    @Test
    @DisplayName("Los comentarios de un show se proyectan a comment y rating")
    void findCommentsOfShowProjectsSummary() {
        when(commentRepository.findByShowIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(
                new CommentDocument("a", 1L, "Muy buena.", 5, Instant.now()),
                new CommentDocument("b", 1L, "Regular.", 3, Instant.now())));

        assertThat(commentService.findCommentsOf(1L))
                .containsExactly(new CommentSummary("Muy buena.", 5), new CommentSummary("Regular.", 3));
    }

    @Test
    @DisplayName("Los comentarios de varios shows se agrupan por show en una sola consulta")
    void findCommentsOfSeveralShowsGroupsByShow() {
        when(commentRepository.findByShowIdInOrderByCreatedAtDesc(List.of(1L, 2993L))).thenReturn(List.of(
                new CommentDocument("a", 1L, "Muy buena.", 5, Instant.now()),
                new CommentDocument("b", 2993L, "Imperdible.", 5, Instant.now()),
                new CommentDocument("c", 1L, "Regular.", 3, Instant.now())));

        Map<Long, List<CommentSummary>> comments = commentService.findCommentsOf(List.of(1L, 2993L));

        assertThat(comments).hasSize(2);
        assertThat(comments.get(1L)).extracting(CommentSummary::comment)
                .containsExactly("Muy buena.", "Regular.");
        assertThat(comments.get(2993L)).extracting(CommentSummary::rating).containsExactly(5);
    }

    @Test
    @DisplayName("Sin ids no se consulta la base")
    void findCommentsOfEmptyCollectionSkipsTheQuery() {
        assertThat(commentService.findCommentsOf(List.of())).isEmpty();

        verifyNoInteractions(commentRepository);
    }
}
