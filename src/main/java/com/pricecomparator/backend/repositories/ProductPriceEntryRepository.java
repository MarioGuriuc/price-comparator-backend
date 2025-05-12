package com.pricecomparator.backend.repositories;

import com.pricecomparator.backend.entities.ProductPriceEntry;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPriceEntryRepository extends MongoRepository<ProductPriceEntry, ObjectId> {
    Optional<ProductPriceEntry> findByProductIdAndStoreIdAndDateAndStoreProductSku(
            ObjectId productId, ObjectId storeId, LocalDate date, String storeProductSku);

    List<ProductPriceEntry> findByProductIdAndStoreIdOrderByDateDesc(
            ObjectId productId, ObjectId storeId);

    List<ProductPriceEntry> findByProductIdAndDate(ObjectId productId, LocalDate date);
}
