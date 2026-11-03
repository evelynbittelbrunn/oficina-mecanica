package br.com.oficina.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
public abstract class IntegrationTest {

    protected static final String ADMIN = "admin@oficina.com";
    protected static final String ADMIN_PASSWORD = "admin-test-password";
    protected static final String ATTENDANT = "atendente@oficina.com";
    protected static final String ATTENDANT_PASSWORD = "attendant-test-password";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String adminToken() throws Exception {
        return login(ADMIN, ADMIN_PASSWORD);
    }

    protected String attendantToken() throws Exception {
        return login(ATTENDANT, ATTENDANT_PASSWORD);
    }

    protected String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new Credentials(email, password))))
                .andExpect(status().isOk())
                .andReturn();
        return read(result).get("token").asText();
    }

    protected MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder request, String token) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    protected MockHttpServletRequestBuilder withJson(MockHttpServletRequestBuilder request, String token, Object body)
            throws Exception {
        return authenticated(request, token).contentType(MediaType.APPLICATION_JSON).content(json(body));
    }

    protected MockHttpServletRequestBuilder publicJson(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return request.contentType(MediaType.APPLICATION_JSON).content(json(body));
    }

    protected String json(Object body) throws Exception {
        return body instanceof String text ? text : objectMapper.writeValueAsString(body);
    }

    protected JsonNode read(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private record Credentials(String email, String password) {
    }
}
