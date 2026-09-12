package com.tvmaze.persistence.repository;

import com.tvmaze.persistence.document.ShowDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/** Acceso a la coleccion {@code shows}, que hace las veces de cache del API de TVmaze. */
@Repository
public interface ShowCacheRepository extends MongoRepository<ShowDocument, Long> {
}
