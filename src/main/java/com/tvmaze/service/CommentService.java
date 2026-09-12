package com.tvmaze.service;

import com.tvmaze.persistence.document.CommentDocument;
import com.tvmaze.persistence.repository.CommentRepository;
import com.tvmaze.web.dto.CommentSummary;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Registro y consulta de calificaciones y comentarios ligados a un show.
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

    /**
     * Comentarios de un show, del mas reciente al mas antiguo.
     *
     * @return lista vacia si el show no tiene comentarios.
     */
    public List<CommentSummary> findCommentsOf(long showId) {
        return commentRepository.findByShowIdOrderByCreatedAtDesc(showId).stream()
                .map(CommentSummary::from)
                .toList();
    }

    /**
     * Comentarios de varios shows, agrupados por id de show y resueltos en una sola
     * consulta a MongoDB.
     *
     * @param showIds ids de los shows; puede venir vacio.
     * @return mapa con los shows que tienen comentarios; los demas no aparecen.
     */
    public Map<Long, List<CommentSummary>> findCommentsOf(Collection<Long> showIds) {
        if (showIds.isEmpty()) {
            return Map.of();
        }
        return commentRepository.findByShowIdInOrderByCreatedAtDesc(showIds).stream()
                .collect(Collectors.groupingBy(
                        CommentDocument::showId,
                        LinkedHashMap::new,
                        Collectors.mapping(CommentSummary::from, Collectors.toList())));
    }
}
