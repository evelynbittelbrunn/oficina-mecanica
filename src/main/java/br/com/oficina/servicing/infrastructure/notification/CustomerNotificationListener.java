package br.com.oficina.servicing.infrastructure.notification;

import br.com.oficina.registration.application.CustomerService;
import br.com.oficina.registration.domain.Customer;
import br.com.oficina.servicing.domain.event.QuoteSent;
import br.com.oficina.servicing.domain.event.ServiceOrderEvent;
import br.com.oficina.servicing.domain.event.VehicleDelivered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Policy "when the quote is sent, notify the customer". Sending is simulated with a log entry
 * (no e-mail/SMS provider in the MVP) and only happens after the transaction commits.
 * Also writes every service order event to an audit log.
 */
@Component
class CustomerNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(CustomerNotificationListener.class);
    private static final Locale PT_BR = Locale.of("pt", "BR");

    private final CustomerService customerService;

    CustomerNotificationListener(CustomerService customerService) {
        this.customerService = customerService;
    }

    @TransactionalEventListener
    void audit(ServiceOrderEvent event) {
        log.info("[event] {} {}", event.getClass().getSimpleName(), event);
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    void notifyQuoteSent(QuoteSent event) {
        try {
            Customer customer = customerService.findCustomerById(event.customerId());
            log.info("[simulated notification] To: {} <{}> | Quote for service order {} totaling {} is awaiting approval. "
                            + "Approve or reject at /api/v1/public/service-orders/{}/quote",
                    customer.getName(), customer.getEmail().value(), event.number(),
                    NumberFormat.getCurrencyInstance(PT_BR).format(event.totalAmount()), event.number());
        } catch (RuntimeException e) {
            log.warn("Failed to notify the quote of service order {}: {}", event.number(), e.getMessage());
        }
    }

    @TransactionalEventListener
    void notifyVehicleDelivered(VehicleDelivered event) {
        log.info("[simulated notification] Vehicle of service order {} delivered to the customer.", event.number());
    }
}
