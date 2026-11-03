package br.com.oficina.servicing.domain;

import br.com.oficina.shared.domain.exception.InvalidTransitionException;
import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ServiceOrderElementsTest {

    @ParameterizedTest(name = "{0} -> {1} allowed = {2}")
    @CsvSource({
            "RECEIVED, IN_DIAGNOSIS, true",
            "RECEIVED, AWAITING_APPROVAL, false",
            "IN_DIAGNOSIS, AWAITING_APPROVAL, true",
            "IN_DIAGNOSIS, IN_PROGRESS, false",
            "AWAITING_APPROVAL, IN_PROGRESS, true",
            "AWAITING_APPROVAL, FINISHED, true",
            "AWAITING_APPROVAL, DELIVERED, false",
            "IN_PROGRESS, FINISHED, true",
            "IN_PROGRESS, DELIVERED, false",
            "FINISHED, DELIVERED, true",
            "FINISHED, IN_PROGRESS, false",
            "DELIVERED, RECEIVED, false"
    })
    void stateMachineShouldDefineAllowedTransitions(ServiceOrderStatus from, ServiceOrderStatus to, boolean allowed) {
        assertThat(from.canTransitionTo(to)).isEqualTo(allowed);
    }

    @Test
    void deliveredIsFinalState() {
        assertThat(ServiceOrderStatus.DELIVERED.next()).isEmpty();
    }

    @Test
    void itemsCanOnlyChangeBeforeTheQuote() {
        assertThat(ServiceOrderStatus.RECEIVED.allowsItemChanges()).isTrue();
        assertThat(ServiceOrderStatus.IN_DIAGNOSIS.allowsItemChanges()).isTrue();
        assertThat(ServiceOrderStatus.AWAITING_APPROVAL.allowsItemChanges()).isFalse();
        assertThat(ServiceOrderStatus.IN_PROGRESS.allowsItemChanges()).isFalse();
    }

    @Test
    void numberShouldBeGeneratedInReadableFormat() {
        assertThat(ServiceOrderNumber.generate(2026, 123).value()).isEqualTo("OS-2026-000123");
        assertThat(ServiceOrderNumber.generate(2026, 1234567).value()).isEqualTo("OS-2026-1234567");
        assertThat(new ServiceOrderNumber(" os-2026-000001 ").toString()).isEqualTo("OS-2026-000001");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"OS-26-000001", "OS-2026-01", "2026-000001", "XX-2026-000001"})
    void invalidNumberShouldBeRejected(String value) {
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new ServiceOrderNumber(value));
    }

    @Test
    void sequenceShouldBePositive() {
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> ServiceOrderNumber.generate(2026, 0));
    }

    @Test
    void quoteShouldBeDecidedOnlyOnce() {
        Instant sentAt = Instant.now();
        Quote pending = Quote.generate(Money.of("100"), sentAt);

        Quote approved = pending.approve(sentAt.plusSeconds(60));
        assertThat(approved.status()).isEqualTo(QuoteStatus.APPROVED);
        assertThat(approved.sentAt()).isEqualTo(sentAt);
        assertThat(approved.decidedAt()).isEqualTo(sentAt.plusSeconds(60));

        Quote rejected = pending.reject("Too expensive", sentAt.plusSeconds(60));
        assertThat(rejected.status()).isEqualTo(QuoteStatus.REJECTED);
        assertThat(rejected.rejectionReason()).isEqualTo("Too expensive");

        assertThatExceptionOfType(InvalidTransitionException.class).isThrownBy(() -> approved.reject("x", Instant.now()));
        assertThatExceptionOfType(InvalidTransitionException.class).isThrownBy(() -> rejected.approve(Instant.now()));
    }

    @Test
    void quoteShouldValidateConsistency() {
        Money amount = Money.of("100");
        Instant now = Instant.now();

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Quote(null, QuoteStatus.PENDING, now, null, null));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Quote(amount, null, now, null, null));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Quote(amount, QuoteStatus.PENDING, null, null, null));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Quote(amount, QuoteStatus.APPROVED, now, null, null));
    }

    @Test
    void historyEntryShouldValidateRequiredFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new StatusHistoryEntry(null, null, ServiceOrderStatus.RECEIVED, now, "a", null));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new StatusHistoryEntry(id, null, null, now, "a", null));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new StatusHistoryEntry(id, null, ServiceOrderStatus.RECEIVED, null, "a", null));
        assertThat(new StatusHistoryEntry(id, null, ServiceOrderStatus.RECEIVED, now, null, null).actor())
                .isEqualTo(StatusHistoryEntry.SYSTEM_ACTOR);
        assertThat(new StatusHistoryEntry(id, null, ServiceOrderStatus.RECEIVED, now, " ana ", null).actor())
                .isEqualTo("ana");
    }

    @Test
    void emptyFilterDoesNotRestrictAnything() {
        ServiceOrderFilter filter = ServiceOrderFilter.empty();

        assertThat(filter.status()).isNull();
        assertThat(filter.customerId()).isNull();
        assertThat(filter.openedFrom()).isNull();
        assertThat(filter.openedTo()).isNull();
    }
}
