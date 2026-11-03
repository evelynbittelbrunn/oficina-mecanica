package br.com.oficina.registration;

import br.com.oficina.support.IntegrationTest;
import br.com.oficina.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegistrationIntegrationTest extends IntegrationTest {

    private String token;

    @BeforeEach
    void authenticate() throws Exception {
        token = attendantToken();
    }

    @Test
    void shouldRunFullCustomerAndVehicleCrud() throws Exception {
        String cpf = TestData.cpf();
        String plate = TestData.licensePlate();

        String customerId = read(mockMvc.perform(withJson(post("/api/v1/customers"), token, Map.of(
                        "name", "Maria da Silva",
                        "document", cpf,
                        "email", "maria@email.com",
                        "phone", "(11) 98888-7777")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.document").value(cpf))
                .andExpect(jsonPath("$.documentType").value("CPF"))
                .andReturn()).get("id").asText();

        mockMvc.perform(withJson(put("/api/v1/customers/" + customerId), token, Map.of(
                        "name", "Maria Souza",
                        "email", "maria.souza@email.com",
                        "phone", "11977776666")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria Souza"));

        String vehicleId = read(mockMvc.perform(withJson(post("/api/v1/vehicles"), token, Map.of(
                        "customerId", customerId,
                        "licensePlate", plate.substring(0, 3) + "-" + plate.substring(3),
                        "brand", "Volkswagen",
                        "model", "Gol",
                        "year", 2020)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.licensePlate").value(plate))
                .andReturn()).get("id").asText();

        mockMvc.perform(authenticated(get("/api/v1/vehicles").param("customerId", customerId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(vehicleId));

        mockMvc.perform(withJson(put("/api/v1/vehicles/" + vehicleId), token, Map.of(
                        "brand", "Volkswagen", "model", "Gol G7", "year", 2021)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Gol G7"));

        mockMvc.perform(authenticated(delete("/api/v1/customers/" + customerId), token))
                .andExpect(status().isConflict());

        mockMvc.perform(authenticated(delete("/api/v1/vehicles/" + vehicleId), token))
                .andExpect(status().isNoContent());
        mockMvc.perform(authenticated(delete("/api/v1/customers/" + customerId), token))
                .andExpect(status().isNoContent());
        mockMvc.perform(authenticated(get("/api/v1/customers/" + customerId), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectDuplicatedAndInvalidDocument() throws Exception {
        Map<String, Object> customer = Map.of(
                "name", "João",
                "document", TestData.cpf(),
                "email", "joao@email.com",
                "phone", "11988887777");

        mockMvc.perform(withJson(post("/api/v1/customers"), token, customer)).andExpect(status().isCreated());
        mockMvc.perform(withJson(post("/api/v1/customers"), token, customer))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Já existe um cliente cadastrado com este documento."));

        mockMvc.perform(withJson(post("/api/v1/customers"), token, Map.of(
                        "name", "João",
                        "document", "111.111.111-11",
                        "email", "joao@email.com",
                        "phone", "11988887777")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("CPF ou CNPJ inválido."));
    }

    @Test
    void shouldValidateRequiredFields() throws Exception {
        mockMvc.perform(withJson(post("/api/v1/vehicles"), token, Map.of("licensePlate", "", "year", 1800)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerId").exists())
                .andExpect(jsonPath("$.errors.licensePlate").exists())
                .andExpect(jsonPath("$.errors.year").exists());
    }
}
