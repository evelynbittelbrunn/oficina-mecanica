package br.com.oficina.shared.domain;

import java.time.Instant;

public interface DomainEvent {

    Instant occurredAt();
}
