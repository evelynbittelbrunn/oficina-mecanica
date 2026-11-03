package br.com.oficina.servicing.domain;

import br.com.oficina.shared.domain.exception.ValidationException;

import java.time.Instant;
import java.util.UUID;

public record StatusHistoryEntry(
        UUID id,
        ServiceOrderStatus previousStatus,
        ServiceOrderStatus newStatus,
        Instant changedAt,
        String actor,
        String note
) {

    public static final String SYSTEM_ACTOR = "system";

    public StatusHistoryEntry {
        if (id == null || newStatus == null || changedAt == null) {
            throw new ValidationException("Registro de histórico incompleto.");
        }
        actor = actor == null || actor.isBlank() ? SYSTEM_ACTOR : actor.trim();
    }

    static StatusHistoryEntry record(ServiceOrderStatus previous, ServiceOrderStatus next, String actor,
                                     Instant changedAt, String note) {
        return new StatusHistoryEntry(UUID.randomUUID(), previous, next, changedAt, actor, note);
    }
}
