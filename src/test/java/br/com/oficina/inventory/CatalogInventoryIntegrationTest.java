package br.com.oficina.inventory;

import br.com.oficina.inventory.domain.InventoryItem;
import br.com.oficina.inventory.domain.InventoryItemRepository;
import br.com.oficina.inventory.domain.InventoryItemType;
import br.com.oficina.shared.domain.valueobject.Money;
import br.com.oficina.support.IntegrationTest;
import br.com.oficina.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CatalogInventoryIntegrationTest extends IntegrationTest {

    @Autowired
    private InventoryItemRepository itemRepository;

    private String admin;
    private String attendant;

    @BeforeEach
    void authenticate() throws Exception {
        admin = adminToken();
        attendant = attendantToken();
    }

    @Test
    void adminShouldManageServiceCatalog() throws Exception {
        String id = read(mockMvc.perform(withJson(post("/api/v1/services"), admin, Map.of(
                        "name", "Troca de óleo",
                        "description", "Óleo e filtro",
                        "basePrice", 120,
                        "estimatedMinutes", 45)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.basePrice").value(120.00))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn()).get("id").asText();

        mockMvc.perform(withJson(put("/api/v1/services/" + id), admin, Map.of(
                        "name", "Troca de óleo sintético", "basePrice", 150.5, "estimatedMinutes", 50)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basePrice").value(150.50));

        mockMvc.perform(authenticated(delete("/api/v1/services/" + id), admin)).andExpect(status().isNoContent());

        mockMvc.perform(authenticated(get("/api/v1/services/" + id), attendant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(authenticated(get("/api/v1/services").param("active", "true"), attendant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(id))));
    }

    @Test
    void attendantCannotChangeCatalogOrInventory() throws Exception {
        mockMvc.perform(withJson(post("/api/v1/services"), attendant, Map.of(
                        "name", "X", "basePrice", 1, "estimatedMinutes", 1)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acesso negado"));

        mockMvc.perform(withJson(post("/api/v1/inventory-items/" + UUID.randomUUID() + "/stock-in"), attendant,
                        Map.of("quantity", 1)))
                .andExpect(status().isForbidden());

        mockMvc.perform(authenticated(get("/api/v1/reports/average-execution-time"), attendant))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldControlInventoryStock() throws Exception {
        String code = TestData.code("FLT");
        String id = read(mockMvc.perform(withJson(post("/api/v1/inventory-items"), admin, Map.of(
                        "code", code.toLowerCase(),
                        "name", "Filtro de óleo",
                        "type", "PART",
                        "unitPrice", 35.9,
                        "initialQuantity", 6,
                        "minimumStock", 5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.stockQuantity").value(6))
                .andExpect(jsonPath("$.lowStock").value(false))
                .andReturn()).get("id").asText();

        mockMvc.perform(withJson(post("/api/v1/inventory-items/" + id + "/stock-out"), admin, Map.of("quantity", 2, "note", "Loss")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(4))
                .andExpect(jsonPath("$.lowStock").value(true));

        mockMvc.perform(authenticated(get("/api/v1/inventory-items/low-stock"), attendant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id)));

        mockMvc.perform(withJson(post("/api/v1/inventory-items/" + id + "/stock-out"), admin, Map.of("quantity", 10)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("disponível 4, solicitado 10")));

        mockMvc.perform(withJson(post("/api/v1/inventory-items/" + id + "/stock-in"), admin, Map.of("quantity", 10, "note", "NF 1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(14));

        mockMvc.perform(authenticated(get("/api/v1/inventory-items/" + id + "/movements"), attendant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].type").value("INBOUND"))
                .andExpect(jsonPath("$[0].balanceAfter").value(14));

        mockMvc.perform(withJson(put("/api/v1/inventory-items/" + id), admin, Map.of(
                        "name", "Filtro de óleo premium", "type", "PART", "unitPrice", 39.9, "minimumStock", 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Filtro de óleo premium"))
                .andExpect(jsonPath("$.stockQuantity").value(14));

        mockMvc.perform(withJson(post("/api/v1/inventory-items"), admin, Map.of(
                        "code", code, "name", "Outro", "type", "PART", "unitPrice", 1, "minimumStock", 0)))
                .andExpect(status().isConflict());

        mockMvc.perform(authenticated(delete("/api/v1/inventory-items/" + id), admin)).andExpect(status().isNoContent());
        mockMvc.perform(authenticated(get("/api/v1/inventory-items/" + id), admin)).andExpect(status().isNotFound());
    }

    @Test
    void concurrentItemWriteShouldBeRejectedByVersion() {
        InventoryItem created = InventoryItem.create(TestData.code("CONC"), "Concurrent item", InventoryItemType.PART,
                Money.of("10"), 1);
        itemRepository.save(created);

        InventoryItem firstRead = itemRepository.findById(created.getId()).orElseThrow();
        InventoryItem secondRead = itemRepository.findById(created.getId()).orElseThrow();

        firstRead.update("Changed by transaction A", InventoryItemType.PART, Money.of("11"), 1);
        itemRepository.save(firstRead);

        secondRead.update("Changed by transaction B", InventoryItemType.PART, Money.of("12"), 1);
        assertThatExceptionOfType(OptimisticLockingFailureException.class)
                .isThrownBy(() -> itemRepository.save(secondRead));

        assertThat(itemRepository.findById(created.getId()).orElseThrow().getName()).isEqualTo("Changed by transaction A");
    }
}
