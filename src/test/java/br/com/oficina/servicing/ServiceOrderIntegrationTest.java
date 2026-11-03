package br.com.oficina.servicing;

import br.com.oficina.servicing.domain.ServiceOrder;
import br.com.oficina.servicing.domain.ServiceOrderRepository;
import br.com.oficina.shared.domain.valueobject.Money;
import br.com.oficina.support.IntegrationTest;
import br.com.oficina.support.TestData;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServiceOrderIntegrationTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    private String admin;
    private String attendant;

    @BeforeEach
    void authenticate() throws Exception {
        admin = adminToken();
        attendant = attendantToken();
    }

    @Test
    void fullFlowShouldGoFromOpeningToDeliveryDeductingStock() throws Exception {
        String cpf = createCustomer();
        String plate = TestData.licensePlate();
        String oilChange = createService("Troca de óleo", "120.00");
        String filter = createInventoryItem("35.90", 10);

        JsonNode opened = openOrder(cpf, plate, List.of(Map.of("id", oilChange)), List.of(Map.of("id", filter, "quantity", 2)));
        String id = opened.get("id").asText();
        String number = opened.get("number").asText();
        assertThat(number).matches("OS-\\d{4}-\\d{6}");
        assertThat(opened.get("status").asText()).isEqualTo("RECEIVED");
        assertThat(opened.get("vehicle").get("licensePlate").asText()).isEqualTo(plate);
        assertThat(opened.get("totalAmount").decimalValue()).isEqualByComparingTo("191.80");

        action(id, "start-diagnosis").andExpect(jsonPath("$.status").value("IN_DIAGNOSIS"));
        mockMvc.perform(withJson(post("/api/v1/service-orders/" + id + "/complete-diagnosis"), attendant,
                        Map.of("notes", "Óleo vencido")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AWAITING_APPROVAL"))
                .andExpect(jsonPath("$.quote.status").value("PENDING"))
                .andExpect(jsonPath("$.quote.totalAmount").value(191.80));

        // the customer tracks and approves using number + document, without a JWT
        mockMvc.perform(get("/api/v1/public/service-orders/" + number).param("document", formatCpf(cpf)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AWAITING_APPROVAL"))
                .andExpect(jsonPath("$.history[0].actor").doesNotExist());
        mockMvc.perform(publicJson(post("/api/v1/public/service-orders/" + number + "/quote/approve"), Map.of("document", cpf)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.quote.status").value("APPROVED"));

        mockMvc.perform(authenticated(get("/api/v1/inventory-items/" + filter), admin))
                .andExpect(jsonPath("$.stockQuantity").value(8));
        mockMvc.perform(authenticated(get("/api/v1/inventory-items/" + filter + "/movements"), admin))
                .andExpect(jsonPath("$[0].type").value("OUTBOUND"))
                .andExpect(jsonPath("$[0].reference").value(number));

        action(id, "finish").andExpect(jsonPath("$.status").value("FINISHED"));
        action(id, "deliver").andExpect(jsonPath("$.status").value("DELIVERED"));

        mockMvc.perform(authenticated(get("/api/v1/service-orders/" + id), attendant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history", hasSize(6)))
                .andExpect(jsonPath("$.history[0].newStatus").value("RECEIVED"))
                .andExpect(jsonPath("$.history[0].actor").value(ATTENDANT))
                .andExpect(jsonPath("$.history[3].actor").value("customer"))
                .andExpect(jsonPath("$.history[5].newStatus").value("DELIVERED"))
                .andExpect(jsonPath("$.diagnosisNotes").value("Óleo vencido"));
    }

    @Test
    void rejectionShouldFinishWithoutExecutionAndWithoutMovingStock() throws Exception {
        String cpf = createCustomer();
        String service = createService("Revisão de freios", "200.00");
        String pads = createInventoryItem("189.00", 4);
        JsonNode order = openOrder(cpf, TestData.licensePlate(), List.of(Map.of("id", service)), List.of(Map.of("id", pads)));
        String number = moveToAwaitingApproval(order);

        mockMvc.perform(publicJson(post("/api/v1/public/service-orders/" + number + "/quote/reject"),
                        Map.of("document", cpf, "reason", "Vou fazer em outra oficina")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.quote.status").value("REJECTED"))
                .andExpect(jsonPath("$.quote.rejectionReason").value("Vou fazer em outra oficina"));

        mockMvc.perform(authenticated(get("/api/v1/inventory-items/" + pads), admin))
                .andExpect(jsonPath("$.stockQuantity").value(4));

        mockMvc.perform(publicJson(post("/api/v1/public/service-orders/" + number + "/quote/approve"), Map.of("document", cpf)))
                .andExpect(status().isConflict());
    }

    @Test
    void approvalWithoutStockShouldFailChangingNothing() throws Exception {
        String cpf = createCustomer();
        String service = createService("Troca de pastilhas", "80.00");
        String filter = createInventoryItem("10.00", 5);
        String pads = createInventoryItem("189.00", 1);
        JsonNode order = openOrder(cpf, TestData.licensePlate(), List.of(Map.of("id", service)),
                List.of(Map.of("id", filter, "quantity", 1), Map.of("id", pads, "quantity", 2)));
        String number = moveToAwaitingApproval(order);

        mockMvc.perform(publicJson(post("/api/v1/public/service-orders/" + number + "/quote/approve"), Map.of("document", cpf)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("disponível 1, necessário 2")));

        // rollback: order, quote, balances and movements untouched (including the item that had stock)
        mockMvc.perform(get("/api/v1/public/service-orders/" + number).param("document", cpf))
                .andExpect(jsonPath("$.status").value("AWAITING_APPROVAL"))
                .andExpect(jsonPath("$.quote.status").value("PENDING"));
        mockMvc.perform(authenticated(get("/api/v1/inventory-items/" + filter), admin)).andExpect(jsonPath("$.stockQuantity").value(5));
        mockMvc.perform(authenticated(get("/api/v1/inventory-items/" + pads), admin)).andExpect(jsonPath("$.stockQuantity").value(1));
        mockMvc.perform(authenticated(get("/api/v1/inventory-items/" + filter + "/movements"), admin)).andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(withJson(post("/api/v1/inventory-items/" + pads + "/stock-in"), admin, Map.of("quantity", 5)))
                .andExpect(status().isOk());
        mockMvc.perform(publicJson(post("/api/v1/public/service-orders/" + number + "/quote/approve"), Map.of("document", cpf)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void invalidTransitionsAndItemChangesOutsideStatusShouldReturn409() throws Exception {
        String cpf = createCustomer();
        String service = createService("Diagnóstico eletrônico", "90.00");
        JsonNode order = openOrder(cpf, TestData.licensePlate(), List.of(), List.of());
        String id = order.get("id").asText();

        action(id, "finish", 409).andExpect(jsonPath("$.title").value("Transição de status inválida"));
        action(id, "deliver", 409);

        action(id, "start-diagnosis");
        mockMvc.perform(authenticated(post("/api/v1/service-orders/" + id + "/complete-diagnosis"), attendant))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Inclua ao menos um serviço antes de concluir o diagnóstico."));

        mockMvc.perform(withJson(post("/api/v1/service-orders/" + id + "/services"), attendant, Map.of("id", service, "quantity", 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.services[0].quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(180.00));
        mockMvc.perform(authenticated(delete("/api/v1/service-orders/" + id + "/services/" + service), attendant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.services", hasSize(0)));
        mockMvc.perform(withJson(post("/api/v1/service-orders/" + id + "/services"), attendant, Map.of("id", service)))
                .andExpect(status().isOk());

        mockMvc.perform(authenticated(post("/api/v1/service-orders/" + id + "/complete-diagnosis"), attendant))
                .andExpect(status().isOk());

        mockMvc.perform(withJson(post("/api/v1/service-orders/" + id + "/services"), attendant, Map.of("id", service)))
                .andExpect(status().isConflict());
        action(id, "start-diagnosis", 409);
    }

    @Test
    void openingShouldRegisterNewVehicleAndValidateData() throws Exception {
        String cpf = createCustomer();
        String plate = TestData.licensePlate();

        mockMvc.perform(withJson(post("/api/v1/service-orders"), attendant, Map.of(
                        "customerDocument", cpf, "vehicle", Map.of("licensePlate", plate))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("informe marca, modelo e ano")));

        JsonNode order = openOrder(cpf, plate, List.of(), List.of());
        mockMvc.perform(authenticated(get("/api/v1/vehicles").param("customerId", order.get("customer").get("id").asText()), attendant))
                .andExpect(jsonPath("$[0].licensePlate").value(plate));

        mockMvc.perform(withJson(post("/api/v1/service-orders"), attendant, Map.of(
                        "customerDocument", cpf, "vehicle", Map.of("licensePlate", plate))))
                .andExpect(status().isCreated());

        mockMvc.perform(withJson(post("/api/v1/service-orders"), attendant, Map.of(
                        "customerDocument", createCustomer(), "vehicle", Map.of("licensePlate", plate))))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(withJson(post("/api/v1/service-orders"), attendant, Map.of(
                        "customerDocument", TestData.cpf(), "vehicle", Map.of("licensePlate", plate))))
                .andExpect(status().isNotFound());

        mockMvc.perform(withJson(post("/api/v1/service-orders"), attendant, Map.of("customerDocument", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerDocument").exists())
                .andExpect(jsonPath("$.errors.vehicle").exists());
    }

    @Test
    void shouldListWithFiltersAndPagination() throws Exception {
        String cpf = createCustomer();
        JsonNode first = openOrder(cpf, TestData.licensePlate(), List.of(), List.of());
        openOrder(cpf, TestData.licensePlate(), List.of(), List.of());
        action(first.get("id").asText(), "start-diagnosis");
        String customerId = first.get("customer").get("id").asText();

        mockMvc.perform(authenticated(get("/api/v1/service-orders").param("customerId", customerId), attendant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)));

        mockMvc.perform(authenticated(get("/api/v1/service-orders")
                        .param("customerId", customerId).param("status", "IN_DIAGNOSIS"), attendant))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(first.get("id").asText()));

        mockMvc.perform(authenticated(get("/api/v1/service-orders")
                        .param("customerId", customerId).param("page", "1").param("size", "1"), attendant))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(authenticated(get("/api/v1/service-orders")
                        .param("customerId", customerId).param("from", "2001-01-01").param("to", "2001-12-31"), attendant))
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(authenticated(get("/api/v1/service-orders").param("size", "500"), attendant))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicTrackingShouldRequireTheOwnersDocument() throws Exception {
        String cpf = createCustomer();
        String number = openOrder(cpf, TestData.licensePlate(), List.of(), List.of()).get("number").asText();

        mockMvc.perform(get("/api/v1/public/service-orders/" + number).param("document", cpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(number))
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        mockMvc.perform(get("/api/v1/public/service-orders/" + number).param("document", TestData.cpf()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/public/service-orders/OS-1999-999999").param("document", cpf))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/public/service-orders/" + number).param("document", "123"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/public/service-orders/" + number))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reportShouldComputeOverallAndPerServiceAverageTime() throws Exception {
        String cpf = createCustomer();
        String serviceA = createService("Service A " + UUID.randomUUID(), "100.00");
        String serviceB = createService("Service B " + UUID.randomUUID(), "50.00");

        String order1 = executeUntilFinished(cpf, List.of(serviceA, serviceB));
        String order2 = executeUntilFinished(cpf, List.of(serviceA));

        // pin the execution instants to an isolated period: order 1 = 90 min, order 2 = 30 min
        setExecutionTimes(order1, Instant.parse("2001-03-10T13:00:00Z"), Instant.parse("2001-03-10T14:30:00Z"));
        setExecutionTimes(order2, Instant.parse("2001-03-11T13:00:00Z"), Instant.parse("2001-03-11T13:30:00Z"));

        mockMvc.perform(authenticated(get("/api/v1/reports/average-execution-time")
                        .param("from", "2001-03-01").param("to", "2001-03-31"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall.orderCount").value(2))
                .andExpect(jsonPath("$.overall.averageMinutes").value(60.0))
                .andExpect(jsonPath("$.byService", hasSize(2)))
                .andExpect(jsonPath("$.byService[?(@.serviceId == '" + serviceA + "')].averageMinutes").value(60.0))
                .andExpect(jsonPath("$.byService[?(@.serviceId == '" + serviceA + "')].orderCount").value(2))
                .andExpect(jsonPath("$.byService[?(@.serviceId == '" + serviceB + "')].averageMinutes").value(90.0));

        mockMvc.perform(authenticated(get("/api/v1/reports/average-execution-time")
                        .param("from", "2001-03-11").param("to", "2001-03-11"), admin))
                .andExpect(jsonPath("$.overall.orderCount").value(1))
                .andExpect(jsonPath("$.overall.averageMinutes").value(30.0));

        mockMvc.perform(authenticated(get("/api/v1/reports/average-execution-time")
                        .param("from", "2001-04-01").param("to", "2001-04-30"), admin))
                .andExpect(jsonPath("$.overall.orderCount").value(0))
                .andExpect(jsonPath("$.overall.averageMinutes").doesNotExist());

        mockMvc.perform(authenticated(get("/api/v1/reports/average-execution-time")
                        .param("from", "2001-04-01").param("to", "2001-03-01"), admin))
                .andExpect(status().isBadRequest());
    }

    @Test
    void concurrentOrderWriteShouldBeRejectedByVersion() throws Exception {
        String id = openOrder(createCustomer(), TestData.licensePlate(), List.of(), List.of()).get("id").asText();

        ServiceOrder firstRead = serviceOrderRepository.findById(UUID.fromString(id)).orElseThrow();
        ServiceOrder secondRead = serviceOrderRepository.findById(UUID.fromString(id)).orElseThrow();

        firstRead.startDiagnosis("mechanic-a");
        serviceOrderRepository.save(firstRead);

        secondRead.addService(UUID.randomUUID(), "Service", Money.of("1"), 1);
        assertThatExceptionOfType(OptimisticLockingFailureException.class)
                .isThrownBy(() -> serviceOrderRepository.save(secondRead));

        mockMvc.perform(authenticated(get("/api/v1/service-orders/" + id), attendant))
                .andExpect(jsonPath("$.status").value("IN_DIAGNOSIS"))
                .andExpect(jsonPath("$.services", hasSize(0)));
    }

    private String createCustomer() throws Exception {
        String cpf = TestData.cpf();
        mockMvc.perform(withJson(post("/api/v1/customers"), attendant, Map.of(
                        "name", "Customer " + cpf, "document", cpf, "email", "customer" + cpf + "@email.com",
                        "phone", "11988887777")))
                .andExpect(status().isCreated());
        return cpf;
    }

    private String createService(String name, String price) throws Exception {
        return read(mockMvc.perform(withJson(post("/api/v1/services"), admin, Map.of(
                        "name", name, "basePrice", price, "estimatedMinutes", 60)))
                .andExpect(status().isCreated())
                .andReturn()).get("id").asText();
    }

    private String createInventoryItem(String price, int quantity) throws Exception {
        return read(mockMvc.perform(withJson(post("/api/v1/inventory-items"), admin, Map.of(
                        "code", TestData.code("PC"), "name", "Part " + price, "type", "PART",
                        "unitPrice", price, "initialQuantity", quantity, "minimumStock", 0)))
                .andExpect(status().isCreated())
                .andReturn()).get("id").asText();
    }

    private JsonNode openOrder(String cpf, String plate, List<Map<String, Object>> services, List<Map<String, Object>> parts)
            throws Exception {
        return read(mockMvc.perform(withJson(post("/api/v1/service-orders"), attendant, Map.of(
                        "customerDocument", cpf,
                        "vehicle", Map.of("licensePlate", plate, "brand", "Fiat", "model", "Argo", "year", 2022),
                        "services", services,
                        "parts", parts)))
                .andExpect(status().isCreated())
                .andReturn());
    }

    private String moveToAwaitingApproval(JsonNode order) throws Exception {
        String id = order.get("id").asText();
        action(id, "start-diagnosis");
        action(id, "complete-diagnosis");
        return order.get("number").asText();
    }

    private String executeUntilFinished(String cpf, List<String> services) throws Exception {
        JsonNode order = openOrder(cpf, TestData.licensePlate(),
                services.stream().map(service -> Map.<String, Object>of("id", service)).toList(), List.of());
        String number = moveToAwaitingApproval(order);
        mockMvc.perform(publicJson(post("/api/v1/public/service-orders/" + number + "/quote/approve"),
                Map.of("document", cpf))).andExpect(status().isOk());
        action(order.get("id").asText(), "finish");
        return order.get("id").asText();
    }

    private void setExecutionTimes(String orderId, Instant start, Instant end) {
        UUID id = UUID.fromString(orderId);
        jdbc.update("UPDATE service_order_status_history SET changed_at = ? WHERE service_order_id = ? AND new_status = 'IN_PROGRESS'",
                Timestamp.from(start), id);
        jdbc.update("UPDATE service_order_status_history SET changed_at = ? WHERE service_order_id = ? AND new_status = 'FINISHED'",
                Timestamp.from(end), id);
    }

    private ResultActions action(String id, String action) throws Exception {
        return action(id, action, 200);
    }

    private ResultActions action(String id, String action, int expectedStatus) throws Exception {
        return mockMvc.perform(authenticated(post("/api/v1/service-orders/" + id + "/" + action), attendant))
                .andExpect(status().is(expectedStatus));
    }

    private static String formatCpf(String cpf) {
        return cpf.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }
}
