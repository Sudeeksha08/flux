package com.brightloop.scheduler;

import com.brightloop.invoice.service.InvoiceService;
import com.brightloop.quotation.service.QuotationService;
import com.brightloop.subscription.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Bounded, idempotent scheduler. Every invoice operation is independently transactional. */
@Component
public class BillingScheduler {
    private static final Logger log = LoggerFactory.getLogger(BillingScheduler.class);
    private final SubscriptionService subscriptions;
    private final InvoiceService invoices;
    private final QuotationService quotations;

    public BillingScheduler(SubscriptionService subscriptions, InvoiceService invoices, QuotationService quotations) {
        this.subscriptions = subscriptions;
        this.invoices = invoices;
        this.quotations = quotations;
    }

    @Scheduled(cron = "${brightloop.scheduler.billing-cron:0 15 1 * * *}")
    public void billDueSubscriptions() {
        var due = subscriptions.due(LocalDate.now(), 500);
        int succeeded = 0;
        for (var subscription : due) {
            try {
                invoices.generate(subscription.getId(), subscription.getNextBillingDate());
                succeeded++;
            } catch (Exception ex) {
                // Failure is logged with the durable subscription identifier and retried on the next run.
                log.error("Recurring billing failed for subscription {}", subscription.getId(), ex);
            }
        }
        log.info("Recurring billing run finished: {} due, {} generated or already present", due.size(), succeeded);
    }

    @Scheduled(cron = "${brightloop.scheduler.maintenance-cron:0 30 1 * * *}")
    public void runMaintenance() {
        try { log.info("Expired quotations: {}", quotations.expireDue()); }
        catch (Exception ex) { log.error("Quotation expiry run failed", ex); }
        try { log.info("Expired subscriptions examined: {}", subscriptions.closeExpired(500)); }
        catch (Exception ex) { log.error("Subscription expiry run failed", ex); }
        try { log.info("Invoices marked overdue: {}", invoices.markOverdue(500)); }
        catch (Exception ex) { log.error("Overdue invoice run failed", ex); }
    }
}
