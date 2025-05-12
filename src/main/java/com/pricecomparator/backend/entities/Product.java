package com.pricecomparator.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private ObjectId id;

    private String name;

    private String category;

    private String brand;

    @Field("package_quantity")
    private Double packageQuantity;

    @Field("package_unit")
    private String packageUnit;

    @Indexed(unique = true, name = "unique_product_key_index")
    @Field("unique_key")
    private String uniqueKey;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;
}
