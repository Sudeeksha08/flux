package com.brightloop.plan.entity;

import com.brightloop.common.enums.BillingFrequency;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanAvailabilityTest {
    @Test
    void permitsInclusiveAvailabilityBoundariesAndRejectsOutsideDates() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        Plan plan = new Plan("Annual", new BigDecimal("120.00"), BillingFrequency.MONTHLY,
                1, from, to, false, false, true, true);

        assertThatCode(() -> plan.validateAvailable(from)).doesNotThrowAnyException();
        assertThatCode(() -> plan.validateAvailable(to)).doesNotThrowAnyException();
        assertThatThrownBy(() -> plan.validateAvailable(from.minusDays(1))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> plan.validateAvailable(to.plusDays(1))).isInstanceOf(IllegalStateException.class);
    }
}
