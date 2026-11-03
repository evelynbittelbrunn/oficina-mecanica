package br.com.oficina.servicing.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Service order lifecycle:
 * {@code RECEIVED → IN_DIAGNOSIS → AWAITING_APPROVAL → IN_PROGRESS → FINISHED → DELIVERED}.
 * When the quote is rejected the order goes from AWAITING_APPROVAL straight to FINISHED.
 */
public enum ServiceOrderStatus {
    RECEIVED,
    IN_DIAGNOSIS,
    AWAITING_APPROVAL,
    IN_PROGRESS,
    FINISHED,
    DELIVERED;

    public Set<ServiceOrderStatus> next() {
        return switch (this) {
            case RECEIVED -> EnumSet.of(IN_DIAGNOSIS);
            case IN_DIAGNOSIS -> EnumSet.of(AWAITING_APPROVAL);
            case AWAITING_APPROVAL -> EnumSet.of(IN_PROGRESS, FINISHED);
            case IN_PROGRESS -> EnumSet.of(FINISHED);
            case FINISHED -> EnumSet.of(DELIVERED);
            case DELIVERED -> EnumSet.noneOf(ServiceOrderStatus.class);
        };
    }

    public boolean canTransitionTo(ServiceOrderStatus target) {
        return next().contains(target);
    }

    public boolean allowsItemChanges() {
        return this == RECEIVED || this == IN_DIAGNOSIS;
    }
}
