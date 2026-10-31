package com.brightloop.dashboard;

import java.math.BigDecimal;

public record DashboardSummary(long customers, long products, long activeSubscriptions,
                               long closedSubscriptions, BigDecimal invoicedRevenue,
                               BigDecimal paymentsReceived, BigDecimal outstandingAmount,
                               long pendingInvoices, long overdueInvoices) { }
