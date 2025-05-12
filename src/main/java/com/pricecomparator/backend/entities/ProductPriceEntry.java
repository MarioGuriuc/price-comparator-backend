package com.pricecomparator.backend.entities;

import com.pricecomparator.backend.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product_prices")
@CompoundIndex(name = "product_store_date_sku_idx", def = "{'productId': 1, 'storeId': 1, 'date': 1, 'storeProductSku': 1}", unique = true)
public class ProductPriceEntry {
    @Id
    private ObjectId id;

    @Field("product_id")
    private ObjectId productId;

    @Field("store_id")
    private ObjectId storeId;

    @Field("store_product_sku")
    private String storeProductSku;

    private Double price;

    private Currency currency;

    private LocalDate date;

    @Field("imported_at")
    private LocalDateTime importedAt;
}
