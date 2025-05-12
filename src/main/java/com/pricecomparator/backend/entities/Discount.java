package com.pricecomparator.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "discounts")
public class Discount {
    @Id
    private ObjectId id;

    @Field("product_id")
    private ObjectId productId;

    @Field("store_id")
    private ObjectId storeId;

    @Field("store_product_sku")
    private String storeProductSku;

    @Field("percentage_of_discount")
    private Integer percentage;

    @Field("from_date")
    private LocalDate fromDate;

    @Field("to_date")
    private LocalDate toDate;

    @Field("discount_added_at")
    private LocalDateTime discountAddedAt;
}
