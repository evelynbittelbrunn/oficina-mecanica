package br.com.oficina.shared.api;

import br.com.oficina.inventory.domain.InsufficientStockException;
import br.com.oficina.shared.domain.exception.ConflictException;
import br.com.oficina.shared.domain.exception.InvalidTransitionException;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.exception.UnauthenticatedException;
import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.support.ControllerTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest(GlobalExceptionHandlerTest.ErrorController.class)
@Import(GlobalExceptionHandlerTest.ErrorController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "validation, 400, Dados inválidos",
            "unauthenticated, 401, Não autenticado",
            "access-denied, 403, Acesso negado",
            "not-found, 404, Recurso não encontrado",
            "conflict, 409, Conflito",
            "transition, 409, Transição de status inválida",
            "concurrency, 409, Conflito de concorrência",
            "integrity, 409, Conflito",
            "stock, 422, Regra de negócio violada",
            "unexpected, 500, Erro interno"
    })
    void shouldMapExceptionToProblemDetail(String type, int status, String title) throws Exception {
        mockMvc.perform(get("/api/v1/test-errors/" + type)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void malformedJsonShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/test-errors")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"field\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.detail").value(startsWith("Corpo da requisição inválido")));
    }

    @RestController
    static class ErrorController {

        private static final Map<String, Supplier<RuntimeException>> ERRORS = Map.of(
                "validation", () -> new ValidationException("Placa inválida."),
                "unauthenticated", () -> new UnauthenticatedException("E-mail ou senha inválidos."),
                "access-denied", () -> new AccessDeniedException("denied"),
                "not-found", () -> new NotFoundException("Cliente não encontrado."),
                "conflict", () -> new ConflictException("Documento já cadastrado."),
                "transition", () -> new InvalidTransitionException("OS em RECEIVED."),
                "concurrency", () -> new OptimisticLockingFailureException("version"),
                "integrity", () -> new DataIntegrityViolationException("fk"),
                "stock", () -> new InsufficientStockException("Estoque insuficiente."),
                "unexpected", () -> new IllegalStateException("bug"));

        @PostMapping("/api/v1/test-errors")
        Map<String, Object> echo(@RequestBody Map<String, Object> body) {
            return body;
        }

        @GetMapping("/api/v1/test-errors/{type}")
        String raise(@PathVariable String type) {
            throw ERRORS.get(type).get();
        }
    }
}
