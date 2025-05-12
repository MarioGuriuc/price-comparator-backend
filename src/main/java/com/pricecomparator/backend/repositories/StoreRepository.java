package com.pricecomparator.backend.repositories;

import com.pricecomparator.backend.entities.Store;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StoreRepository extends MongoRepository<Store, ObjectId> {
    Optional<Store> findByName(String name);
}
