package com.pricecomparator.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_price_alerts")
public class UserPriceAlert {
    @Id
    private ObjectId id;

    @Field("user_identifier")
    private String userIdentifier;

    @Field("product_id")
    private ObjectId productId;

    @Field("target_price")
    private Double targetPrice;

    @Field("store_id")
    private ObjectId storeId;

    @Field("is_active")
    private boolean isActive;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("last_checked_at")
    private LocalDateTime lastCheckedAt;
}
