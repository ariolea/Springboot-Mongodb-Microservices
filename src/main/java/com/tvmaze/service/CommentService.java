package com.tvmaze.service;

import com.tvmaze.persistence.document.CommentDocument;
import com.tvmaze.persistence.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Registro de calificaciones y comentarios ligados a un show.
 */
@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Guarda el comentario en MongoDB ligado al id del show.
     *
     * @param showId  show al que pertenece el comentario.
     * @param comment texto del comentario.
     * @param rating  calificacion de 0 a 5.
     * @return el documento guardado, ya con el id asignado por MongoDB.
     */
    public CommentDocument saveComment(long showId, String comment, int rating) {
        CommentDocument saved = commentRepository.save(CommentDocument.of(showId, comment.trim(), rating));
        log.info("Comentario {} registrado para el show {} con calificacion {}.",
                saved.id(), showId, rating);
        return saved;
    }

}
