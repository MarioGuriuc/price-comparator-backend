package com.pricecomparator.backend.repositories;

import com.pricecomparator.backend.entities.Discount;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiscountRepository extends MongoRepository<Discount, ObjectId> {

    List<Discount> findByProductIdAndStoreIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            ObjectId productId, ObjectId storeId, LocalDate date1, LocalDate date2);

    @Query("{ 'fromDate': { '$lte': ?0 }, 'toDate': { '$gte': ?0 } }")
    List<Discount> findActiveDiscounts(LocalDate currentDate);

    List<Discount> findByDiscountAddedAtAfter(LocalDateTime discountAddedAt);
}
