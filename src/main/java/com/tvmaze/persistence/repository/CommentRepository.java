package com.tvmaze.persistence.repository;

import com.tvmaze.persistence.document.CommentDocument;
import java.util.Collection;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/** Acceso a la coleccion {@code comments}. */
@Repository
public interface CommentRepository extends MongoRepository<CommentDocument, String> {

    /** Comentarios de un show, del mas reciente al mas antiguo. */
    List<CommentDocument> findByShowIdOrderByCreatedAtDesc(Long showId);

    /**
     * Comentarios de varios shows en una sola consulta, para no disparar una
     * consulta por cada resultado de la busqueda.
     */
    List<CommentDocument> findByShowIdInOrderByCreatedAtDesc(Collection<Long> showIds);
}
