package com.brightloop.dashboard;

import com.brightloop.common.enums.InvoiceStatus;
import com.brightloop.common.enums.SubscriptionStatus;
import com.brightloop.customer.repository.CustomerRepository;
import com.brightloop.invoice.repository.InvoiceRepository;
import com.brightloop.plan.repository.PlanRepository;
import com.brightloop.product.repository.ProductRepository;
import com.brightloop.subscription.repository.SubscriptionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class DashboardController {
    private final CustomerRepository customers;
    private final ProductRepository products;
    private final SubscriptionRepository subscriptions;
    private final InvoiceRepository invoices;

    public DashboardController(CustomerRepository customers, ProductRepository products,
                               SubscriptionRepository subscriptions, InvoiceRepository invoices) {
        this.customers = customers;
        this.products = products;
        this.subscriptions = subscriptions;
        this.invoices = invoices;
    }

    @GetMapping("/summary")
    public DashboardSummary summary() {
        return new DashboardSummary(customers.count(), products.count(),
                subscriptions.countByStatus(SubscriptionStatus.ACTIVE),
                subscriptions.countByStatus(SubscriptionStatus.CLOSED),
                invoices.sumInvoicedRevenue(), invoices.sumCollectedPayments(), invoices.sumOutstanding(),
                invoices.countByStatus(InvoiceStatus.DRAFT) + invoices.countByStatus(InvoiceStatus.CONFIRMED)
                        + invoices.countByStatus(InvoiceStatus.PARTIALLY_PAID),
                invoices.countByStatus(InvoiceStatus.OVERDUE));
    }
}
