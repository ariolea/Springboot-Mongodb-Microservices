package com.tvmaze.persistence.repository;

import com.tvmaze.persistence.document.CommentDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/** Acceso a la coleccion {@code comments}. */
@Repository
public interface CommentRepository extends MongoRepository<CommentDocument, String> {
}
