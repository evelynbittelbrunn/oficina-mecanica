package br.com.oficina.servicing.domain.event;

import br.com.oficina.shared.domain.DomainEvent;

import java.util.UUID;

public sealed interface ServiceOrderEvent extends DomainEvent
        permits ServiceOrderOpened,
                DiagnosisStarted,
                QuoteSent,
                QuoteApproved,
                QuoteRejected,
                ExecutionStarted,
                ExecutionFinished,
                VehicleDelivered {

    UUID serviceOrderId();

    String number();
}
