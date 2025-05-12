package com.pricecomparator.backend.services;

import com.pricecomparator.backend.entities.Discount;
import com.pricecomparator.backend.entities.Product;
import com.pricecomparator.backend.entities.ProductPriceEntry;
import com.pricecomparator.backend.entities.Store;
import com.pricecomparator.backend.enums.Currency;
import com.pricecomparator.backend.repositories.DiscountRepository;
import com.pricecomparator.backend.repositories.ProductRepository;
import com.pricecomparator.backend.repositories.ProductPriceEntryRepository;
import com.pricecomparator.backend.repositories.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setDelimiter(',')
            .setTrim(true)
            .get();

    private final ProductRepository productRepository;
    private final ProductPriceEntryRepository productPriceEntryRepository;
    private final DiscountRepository discountRepository;
    private final StoreRepository storeRepository;

    @Override
    public void run(String... args) {
        log.info("Checking if data initialization is necessary...");
        if (storeRepository.count() > 0 && productRepository.count() > 0) {
            log.info("Database seems populated. Skipping CSV initialization.");
            return;
        }
        log.info("Initializing data from CSV files...");

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:data/*.csv");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null) continue;

                log.info("Processing file: {}", filename);
                if (filename.contains("_discounts_")) {
                    parseDiscountCsv(resource);
                }
                else if (filename.matches("^[a-zA-Z0-9]+_\\d{4}-\\d{2}-\\d{2}\\.csv$")) {
                    parseProductPriceCsv(resource);
                }
                else {
                    log.warn("Unknown file name format, skipping: {}", filename);
                }
            }
        }
        catch (Exception e) {
            log.error("Error loading CSV files from classpath:data/", e);
        }

        log.info("Data initialization finished.");
    }

    private Store findOrCreateStore(String storeName) {
        Optional<Store> existingStoreOpt = storeRepository.findByName(storeName);
        if (existingStoreOpt.isPresent()) {
            return existingStoreOpt.get();
        }
        else {
            Store newStore = new Store();
            newStore.setName(storeName);
            Store savedStore = storeRepository.save(newStore);
            log.info("New store created: {}", savedStore.getName());
            return savedStore;
        }
    }

    private Product findOrCreateProduct(String uniqueKey, CSVRecord csvRecord) {
        Optional<Product> existingProductOpt = productRepository.findByUniqueKey(uniqueKey);
        Product product;
        if (existingProductOpt.isPresent()) {
            product = existingProductOpt.get();
        }
        else {
            product = new Product();
            product.setName(csvRecord.get("product_name"));
            product.setCategory(csvRecord.get("product_category"));
            product.setBrand(csvRecord.get("brand"));
            product.setPackageQuantity(Double.parseDouble(csvRecord.get("package_quantity")));
            product.setPackageUnit(csvRecord.get("package_unit"));
            product.setUniqueKey(uniqueKey);
            product.setCreatedAt(LocalDateTime.now());
            product = productRepository.save(product);
            log.debug("New product created: {}", product.getName());
        }
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    private String generateProductUniqueKey(String productName, String brand, double packageQuantity, String packageUnit) {
        return (productName + "_" + brand + "_" + packageQuantity + "_" + packageUnit)
                .toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_.-]", "");
    }

    private void processCSVFile(Resource resource, String fileType, BiConsumer<CSVRecord, ProcessingContext> processor) {
        String filename = resource.getFilename();
        if (filename == null) {
            log.warn("Null filename encountered, skipping");
            return;
        }

        ProcessingContext context = new ProcessingContext();
        context.filename = filename;

        if (fileType.equals("price")) {
            if (!filename.matches("^[a-zA-Z0-9]+_\\d{4}-\\d{2}-\\d{2}\\.csv$")) {
                log.warn("Invalid price file name: {}", filename);
                return;
            }

            String[] parts = filename.replace(".csv", "").split("_");
            if (parts.length < 2) {
                log.warn("Invalid price file name (cannot extract store name/date): {}", filename);
                return;
            }
            context.storeName = parts[0];
            try {
                context.date = LocalDate.parse(parts[parts.length - 1], DATE_FORMATTER);
            }
            catch (Exception e) {
                log.warn("Cannot extract date from price file name: {}. Error: {}", filename, e.getMessage());
                return;
            }
        }
        else if (fileType.equals("discount")) {
            if (!filename.contains("_discounts_")) {
                log.warn("Invalid discount file name: {}", filename);
                return;
            }
            context.storeName = filename.substring(0, filename.indexOf("_discounts_"));
        }

        context.store = findOrCreateStore(context.storeName);

        try {
            parseCsv(resource, context, processor);
        }
        catch (Exception e) {
            log.error("Error parsing {} CSV {}: {}", fileType, filename, e.getMessage());
        }
    }

    private void parseCsv(Resource resource, ProcessingContext context, BiConsumer<CSVRecord, ProcessingContext> processor) throws IOException {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csvParser = CSV_FORMAT.parse(reader)) {

            for (CSVRecord csvRecord : csvParser) {
                try {
                    processor.accept(csvRecord, context);
                }
                catch (Exception e) {
                    log.error("Error processing a row from {}: {}. Row: {}",
                            context.filename, e.getMessage(), csvRecord.toString());
                }
            }
        }
    }

    private ProductCsvFields extractProductCsvFields(CSVRecord csvRecord) {
        ProductCsvFields fields = new ProductCsvFields();
        fields.productName = csvRecord.get("product_name");
        fields.brand = csvRecord.get("brand");
        fields.packageQuantity = Double.parseDouble(csvRecord.get("package_quantity"));
        fields.packageUnit = csvRecord.get("package_unit");
        fields.storeProductSku = csvRecord.get("product_id");
        return fields;
    }

    private void parseProductPriceCsv(Resource resource) {
        processCSVFile(resource, "price", (csvRecord, context) -> {
            ProductCsvFields fields = extractProductCsvFields(csvRecord);

            String uniqueKey = generateProductUniqueKey(fields.productName, fields.brand, fields.packageQuantity, fields.packageUnit);
            Product product = findOrCreateProduct(uniqueKey, csvRecord);

            Optional<ProductPriceEntry> existingEntry = productPriceEntryRepository.findByProductIdAndStoreIdAndDateAndStoreProductSku(
                    product.getId(), context.store.getId(), context.date, fields.storeProductSku);

            if (existingEntry.isEmpty()) {
                ProductPriceEntry ppe = new ProductPriceEntry();
                ppe.setProductId(product.getId());
                ppe.setStoreId(context.store.getId());
                ppe.setStoreProductSku(fields.storeProductSku);
                ppe.setPrice(Double.parseDouble(csvRecord.get("price")));
                ppe.setCurrency(Currency.valueOf(csvRecord.get("currency")));
                ppe.setDate(context.date);
                ppe.setImportedAt(LocalDateTime.now());
                productPriceEntryRepository.save(ppe);
            }
            else {
                log.trace("Price entry already exists for product {} at store {} on date {}. Skipping.",
                        uniqueKey, context.storeName, context.date);
            }
        });
    }

    private void parseDiscountCsv(Resource resource) {
        processCSVFile(resource, "discount", (csvRecord, context) -> {
            ProductCsvFields fields = extractProductCsvFields(csvRecord);

            String uniqueKey = generateProductUniqueKey(fields.productName, fields.brand, fields.packageQuantity, fields.packageUnit);
            Product product = findOrCreateProduct(uniqueKey, csvRecord);

            Discount discount = new Discount();
            discount.setProductId(product.getId());
            discount.setStoreId(context.store.getId());
            discount.setStoreProductSku(fields.storeProductSku);
            discount.setPercentage(Integer.parseInt(csvRecord.get("percentage_of_discount")));
            discount.setFromDate(LocalDate.parse(csvRecord.get("from_date"), DATE_FORMATTER));
            discount.setToDate(LocalDate.parse(csvRecord.get("to_date"), DATE_FORMATTER));
            discount.setDiscountAddedAt(LocalDateTime.now());
            discountRepository.save(discount);
        });
    }

    private static class ProcessingContext {
        String filename;
        String storeName;
        LocalDate date;
        Store store;
    }

    private static class ProductCsvFields {
        String productName;
        String brand;
        double packageQuantity;
        String packageUnit;
        String storeProductSku;
    }
}
