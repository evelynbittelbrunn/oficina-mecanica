package br.com.oficina.servicing.domain;

import br.com.oficina.servicing.domain.event.DiagnosisStarted;
import br.com.oficina.servicing.domain.event.ExecutionFinished;
import br.com.oficina.servicing.domain.event.ExecutionStarted;
import br.com.oficina.servicing.domain.event.QuoteApproved;
import br.com.oficina.servicing.domain.event.QuoteRejected;
import br.com.oficina.servicing.domain.event.QuoteSent;
import br.com.oficina.servicing.domain.event.ServiceOrderOpened;
import br.com.oficina.servicing.domain.event.VehicleDelivered;
import br.com.oficina.shared.domain.DomainEvent;
import br.com.oficina.shared.domain.exception.BusinessRuleException;
import br.com.oficina.shared.domain.exception.InvalidTransitionException;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;

class ServiceOrderTest {

    private static final ServiceOrderNumber NUMBER = ServiceOrderNumber.generate(2026, 123);
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VEHICLE_ID = UUID.randomUUID();
    private static final UUID OIL_CHANGE = UUID.randomUUID();
    private static final UUID ALIGNMENT = UUID.randomUUID();
    private static final UUID FILTER = UUID.randomUUID();
    private static final String ACTOR = "atendente@oficina.com";

    @Test
    void shouldOpenReceivedOrderWithHistoryAndEvent() {
        ServiceOrder order = ServiceOrder.open(NUMBER, CUSTOMER_ID, VEHICLE_ID, ACTOR);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getNumber()).isEqualTo(NUMBER);
        assertThat(order.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(order.getVehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(order.getOpenedAt()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        assertThat(order.getQuote()).isNull();
        assertThat(order.getVersion()).isNull();
        assertThat(order.getHistory())
                .extracting(StatusHistoryEntry::previousStatus, StatusHistoryEntry::newStatus, StatusHistoryEntry::actor)
                .containsExactly(tuple(null, ServiceOrderStatus.RECEIVED, ACTOR));

        List<DomainEvent> events = order.pullEvents();
        assertThat(events).singleElement().isInstanceOf(ServiceOrderOpened.class);
        ServiceOrderOpened opened = (ServiceOrderOpened) events.getFirst();
        assertThat(opened.number()).isEqualTo("OS-2026-000123");
        assertThat(opened.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(opened.vehicleId()).isEqualTo(VEHICLE_ID);
    }

    @Test
    void shouldRequireMandatoryDataWhenOpening() {
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> ServiceOrder.open(null, CUSTOMER_ID, VEHICLE_ID, ACTOR));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> ServiceOrder.open(NUMBER, null, VEHICLE_ID, ACTOR));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> ServiceOrder.open(NUMBER, CUSTOMER_ID, null, ACTOR));
    }

    @Test
    void shouldGoThroughFullFlowUntilDelivery() {
        ServiceOrder order = openWithItems();
        order.pullEvents();

        order.startDiagnosis("mecanico@oficina.com");
        order.completeDiagnosis("  Óleo vencido e filtro saturado  ", "mecanico@oficina.com");
        order.approveQuote("customer");
        order.finishExecution("mecanico@oficina.com");
        order.deliver(ACTOR);

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.DELIVERED);
        assertThat(order.getDiagnosisNotes()).isEqualTo("Óleo vencido e filtro saturado");
        assertThat(order.getQuote().status()).isEqualTo(QuoteStatus.APPROVED);
        assertThat(order.getQuote().decidedAt()).isNotNull();

        assertThat(order.getHistory())
                .extracting(StatusHistoryEntry::previousStatus, StatusHistoryEntry::newStatus)
                .containsExactly(
                        tuple(null, ServiceOrderStatus.RECEIVED),
                        tuple(ServiceOrderStatus.RECEIVED, ServiceOrderStatus.IN_DIAGNOSIS),
                        tuple(ServiceOrderStatus.IN_DIAGNOSIS, ServiceOrderStatus.AWAITING_APPROVAL),
                        tuple(ServiceOrderStatus.AWAITING_APPROVAL, ServiceOrderStatus.IN_PROGRESS),
                        tuple(ServiceOrderStatus.IN_PROGRESS, ServiceOrderStatus.FINISHED),
                        tuple(ServiceOrderStatus.FINISHED, ServiceOrderStatus.DELIVERED));
        assertThat(order.getHistory()).extracting(StatusHistoryEntry::actor)
                .containsExactly(ACTOR, "mecanico@oficina.com", "mecanico@oficina.com", "customer", "mecanico@oficina.com", ACTOR);

        assertThat(order.pullEvents()).extracting(event -> event.getClass().getSimpleName())
                .containsExactly(
                        DiagnosisStarted.class.getSimpleName(),
                        QuoteSent.class.getSimpleName(),
                        QuoteApproved.class.getSimpleName(),
                        ExecutionStarted.class.getSimpleName(),
                        ExecutionFinished.class.getSimpleName(),
                        VehicleDelivered.class.getSimpleName());
    }

    @Test
    void completingDiagnosisShouldGeneratePendingQuoteWithItemsTotal() {
        ServiceOrder order = openWithItems();
        order.startDiagnosis(ACTOR);
        order.pullEvents();

        order.completeDiagnosis(null, ACTOR);

        // 2 x 120.00 (oil change) + 1 x 150.00 (alignment) + 3 x 35.90 (filter) = 497.70
        Quote quote = order.getQuote();
        assertThat(quote.totalAmount()).isEqualTo(Money.of("497.70"));
        assertThat(quote.status()).isEqualTo(QuoteStatus.PENDING);
        assertThat(quote.sentAt()).isNotNull();
        assertThat(quote.decidedAt()).isNull();
        assertThat(order.getDiagnosisNotes()).isNull();
        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);

        QuoteSent sent = (QuoteSent) order.pullEvents().getFirst();
        assertThat(sent.totalAmount()).isEqualByComparingTo(new BigDecimal("497.70"));
        assertThat(sent.customerId()).isEqualTo(CUSTOMER_ID);
    }

    @Test
    void rejectionShouldFinishWithoutExecutionRecordingReason() {
        ServiceOrder order = orderIn(ServiceOrderStatus.AWAITING_APPROVAL);
        order.pullEvents();

        order.rejectQuote("  Valor acima do esperado  ", "customer");

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.FINISHED);
        assertThat(order.getQuote().status()).isEqualTo(QuoteStatus.REJECTED);
        assertThat(order.getQuote().rejectionReason()).isEqualTo("Valor acima do esperado");
        assertThat(order.getHistory().getLast().note()).isEqualTo("Orçamento rejeitado: Valor acima do esperado");
        assertThat(order.pullEvents()).singleElement()
                .isInstanceOfSatisfying(QuoteRejected.class,
                        event -> assertThat(event.reason()).isEqualTo("Valor acima do esperado"));

        order.deliver(ACTOR);
        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.DELIVERED);
    }

    @Test
    void shouldNotCompleteDiagnosisWithoutService() {
        ServiceOrder order = ServiceOrder.open(NUMBER, CUSTOMER_ID, VEHICLE_ID, ACTOR);
        order.addPart(FILTER, "Filtro", Money.of("35.90"), 1);
        order.startDiagnosis(ACTOR);

        assertThatExceptionOfType(BusinessRuleException.class)
                .isThrownBy(() -> order.completeDiagnosis(null, ACTOR))
                .withMessage("Inclua ao menos um serviço antes de concluir o diagnóstico.");
        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.IN_DIAGNOSIS);
        assertThat(order.getQuote()).isNull();
    }

    @Test
    void shouldValidateDecisionTexts() {
        ServiceOrder inDiagnosis = orderIn(ServiceOrderStatus.IN_DIAGNOSIS);
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> inDiagnosis.completeDiagnosis("a".repeat(2001), ACTOR));

        ServiceOrder awaiting = orderIn(ServiceOrderStatus.AWAITING_APPROVAL);
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> awaiting.rejectQuote(" ", ACTOR));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> awaiting.rejectQuote(null, ACTOR));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> awaiting.rejectQuote("a".repeat(501), ACTOR));
        assertThat(awaiting.getStatus()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
    }

    @Test
    void historyShouldUseSystemActorWhenMissing() {
        ServiceOrder order = ServiceOrder.open(NUMBER, CUSTOMER_ID, VEHICLE_ID, " ");

        assertThat(order.getHistory().getFirst().actor()).isEqualTo(StatusHistoryEntry.SYSTEM_ACTOR);
    }

    static Stream<Arguments> operationsOutsideExpectedStatus() {
        List<Operation> operations = List.of(
                new Operation("startDiagnosis", ServiceOrderStatus.RECEIVED, order -> order.startDiagnosis(ACTOR)),
                new Operation("completeDiagnosis", ServiceOrderStatus.IN_DIAGNOSIS, order -> order.completeDiagnosis(null, ACTOR)),
                new Operation("approveQuote", ServiceOrderStatus.AWAITING_APPROVAL, order -> order.approveQuote(ACTOR)),
                new Operation("rejectQuote", ServiceOrderStatus.AWAITING_APPROVAL, order -> order.rejectQuote("Too expensive", ACTOR)),
                new Operation("finishExecution", ServiceOrderStatus.IN_PROGRESS, order -> order.finishExecution(ACTOR)),
                new Operation("deliver", ServiceOrderStatus.FINISHED, order -> order.deliver(ACTOR)));

        return operations.stream().flatMap(operation -> Arrays.stream(ServiceOrderStatus.values())
                .filter(status -> status != operation.expectedStatus())
                .map(status -> Arguments.of(operation.name(), status, operation.action())));
    }

    @ParameterizedTest(name = "{0} with order in {1} must be rejected")
    @MethodSource("operationsOutsideExpectedStatus")
    void shouldRejectTransitionOutsideExpectedStatus(String name, ServiceOrderStatus status, Consumer<ServiceOrder> action) {
        ServiceOrder order = orderIn(status);
        int entriesBefore = order.getHistory().size();
        order.pullEvents();

        assertThatExceptionOfType(InvalidTransitionException.class)
                .isThrownBy(() -> action.accept(order))
                .withMessageContaining("a OS está em " + status);

        assertThat(order.getStatus()).isEqualTo(status);
        assertThat(order.getHistory()).hasSize(entriesBefore);
        assertThat(order.pullEvents()).isEmpty();
    }

    @Nested
    class Items {

        @Test
        void shouldAddServicesAndPartsWithCurrentPrice() {
            ServiceOrder order = openWithItems();

            assertThat(order.getServiceItems())
                    .extracting(ServiceItem::getServiceId, ServiceItem::getName, ServiceItem::getUnitPrice, ServiceItem::getQuantity)
                    .containsExactly(
                            tuple(OIL_CHANGE, "Troca de óleo", Money.of("120.00"), 2),
                            tuple(ALIGNMENT, "Alinhamento", Money.of("150.00"), 1));
            assertThat(order.getPartItems())
                    .extracting(PartItem::getInventoryItemId, PartItem::getQuantity)
                    .containsExactly(tuple(FILTER, 3));
            assertThat(order.getServiceItems().getFirst().getId()).isNotNull();
            assertThat(order.getServiceItems().getFirst().subtotal()).isEqualTo(Money.of("240.00"));
        }

        @Test
        void addingRepeatedItemShouldIncreaseQuantityKeepingOriginalPrice() {
            ServiceOrder order = ServiceOrder.open(NUMBER, CUSTOMER_ID, VEHICLE_ID, ACTOR);

            order.addService(OIL_CHANGE, "Troca de óleo", Money.of("120.00"), 1);
            order.addService(OIL_CHANGE, "Troca de óleo", Money.of("999.00"), 2);
            order.addPart(FILTER, "Filtro", Money.of("35.90"), 1);
            order.addPart(FILTER, "Filtro", Money.of("50.00"), 1);

            assertThat(order.getServiceItems()).singleElement().satisfies(item -> {
                assertThat(item.getQuantity()).isEqualTo(3);
                assertThat(item.getUnitPrice()).isEqualTo(Money.of("120.00"));
            });
            assertThat(order.getPartItems()).singleElement().satisfies(item -> assertThat(item.getQuantity()).isEqualTo(2));
        }

        @Test
        void shouldRemoveItems() {
            ServiceOrder order = openWithItems();

            order.removeService(ALIGNMENT);
            order.removePart(FILTER);

            assertThat(order.getServiceItems()).extracting(ServiceItem::getServiceId).containsExactly(OIL_CHANGE);
            assertThat(order.getPartItems()).isEmpty();
            assertThat(order.calculateTotal()).isEqualTo(Money.of("240.00"));
        }

        @Test
        void removingMissingItemShouldFail() {
            ServiceOrder order = openWithItems();

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> order.removeService(UUID.randomUUID()));
            assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> order.removePart(UUID.randomUUID()));
        }

        @Test
        void shouldRejectInvalidQuantity() {
            ServiceOrder order = openWithItems();

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> order.addService(UUID.randomUUID(), "X", Money.ZERO, 0));
            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> order.addService(OIL_CHANGE, "X", Money.ZERO, -1));
            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> order.addPart(FILTER, "X", Money.ZERO, 0));
        }

        @Test
        void shouldAllowItemChangesDuringDiagnosis() {
            ServiceOrder order = orderIn(ServiceOrderStatus.IN_DIAGNOSIS);

            order.addPart(FILTER, "Filtro", Money.of("35.90"), 1);

            assertThat(order.getPartItems()).hasSize(1);
        }

        @ParameterizedTest
        @EnumSource(value = ServiceOrderStatus.class, names = {"AWAITING_APPROVAL", "IN_PROGRESS", "FINISHED", "DELIVERED"})
        void shouldNotAllowItemChangesAfterDiagnosis(ServiceOrderStatus status) {
            ServiceOrder order = orderIn(status);
            Money price = Money.of("10");

            assertThatExceptionOfType(InvalidTransitionException.class).isThrownBy(() -> order.addService(UUID.randomUUID(), "X", price, 1));
            assertThatExceptionOfType(InvalidTransitionException.class).isThrownBy(() -> order.removeService(OIL_CHANGE));
            assertThatExceptionOfType(InvalidTransitionException.class).isThrownBy(() -> order.addPart(UUID.randomUUID(), "X", price, 1));
            assertThatExceptionOfType(InvalidTransitionException.class).isThrownBy(() -> order.removePart(FILTER));
        }

        @Test
        void itemShouldValidateItsData() {
            UUID id = UUID.randomUUID();
            Money price = Money.of("10");

            assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new ServiceItem(null, id, "X", price, 1));
            assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new ServiceItem(id, null, "X", price, 1));
            assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new PartItem(id, id, " ", price, 1));
            assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new PartItem(id, id, null, price, 1));
            assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new PartItem(id, id, "X", null, 1));
        }
    }

    @Test
    void reconstituteShouldNotRegisterEvents() {
        ServiceOrder original = orderIn(ServiceOrderStatus.AWAITING_APPROVAL);

        ServiceOrder rebuilt = ServiceOrder.reconstitute(original.getId(), original.getNumber(), CUSTOMER_ID,
                VEHICLE_ID, original.getOpenedAt(), original.getStatus(), original.getServiceItems(),
                original.getPartItems(), original.getQuote(), "notes", original.getHistory(), 3L);

        assertThat(rebuilt.pullEvents()).isEmpty();
        assertThat(rebuilt.getVersion()).isEqualTo(3L);
        assertThat(rebuilt.calculateTotal()).isEqualTo(original.calculateTotal());
        assertThat(rebuilt.getHistory()).isEqualTo(original.getHistory());

        rebuilt.approveQuote("customer");
        assertThat(rebuilt.getStatus()).isEqualTo(ServiceOrderStatus.IN_PROGRESS);
    }

    @Test
    void reconstituteShouldRequireStatus() {
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> ServiceOrder.reconstitute(
                UUID.randomUUID(), NUMBER, CUSTOMER_ID, VEHICLE_ID, Instant.now(), null,
                List.of(), List.of(), null, null, List.of(), null));
    }

    private static ServiceOrder openWithItems() {
        ServiceOrder order = ServiceOrder.open(NUMBER, CUSTOMER_ID, VEHICLE_ID, ACTOR);
        order.addService(OIL_CHANGE, "Troca de óleo", Money.of("120.00"), 2);
        order.addService(ALIGNMENT, "Alinhamento", Money.of("150.00"), 1);
        order.addPart(FILTER, "Filtro de óleo", Money.of("35.90"), 3);
        return order;
    }

    static ServiceOrder orderIn(ServiceOrderStatus status) {
        ServiceOrder order = openWithItems();
        if (status == ServiceOrderStatus.RECEIVED) {
            return order;
        }
        order.startDiagnosis(ACTOR);
        if (status == ServiceOrderStatus.IN_DIAGNOSIS) {
            return order;
        }
        order.completeDiagnosis("Diagnosis", ACTOR);
        if (status == ServiceOrderStatus.AWAITING_APPROVAL) {
            return order;
        }
        order.approveQuote("customer");
        if (status == ServiceOrderStatus.IN_PROGRESS) {
            return order;
        }
        order.finishExecution(ACTOR);
        if (status == ServiceOrderStatus.FINISHED) {
            return order;
        }
        order.deliver(ACTOR);
        return order;
    }

    private record Operation(String name, ServiceOrderStatus expectedStatus, Consumer<ServiceOrder> action) {
    }
}
