package com.brightloop.product.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class ProductVariantTest {
    @Test
    void calculatesBaseAndVariantPriceWithDecimalPrecision() {
        Product product = new Product("Plan", "SAAS", new BigDecimal("1000.00"), BigDecimal.ZERO, null);
        ProductVariant variant = new ProductVariant("Odoo", new BigDecimal("560.00"));

        assertThat(variant.finalPrice(product)).isEqualByComparingTo("1560.00");
    }
}
