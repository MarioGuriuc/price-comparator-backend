package com.pricecomparator.backend.enums;

import lombok.*;

@AllArgsConstructor
@Getter
public enum Currency {
    EUR("EUR"),
    RON("RON");

    private final String value;
}
