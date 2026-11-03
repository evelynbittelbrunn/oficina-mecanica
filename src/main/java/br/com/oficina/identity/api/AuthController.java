package br.com.oficina.identity.api;

import br.com.oficina.identity.api.dto.LoginRequest;
import br.com.oficina.identity.api.dto.LoginResponse;
import br.com.oficina.identity.application.AuthenticationService;
import br.com.oficina.identity.application.dto.AccessToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "JWT issuing for internal users")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Log in", description = "Checks e-mail and password and returns a JWT (Bearer).")
    @ApiResponse(responseCode = "200", description = "Authenticated")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Invalid e-mail or password")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AccessToken token = authenticationService.authenticate(request.email(), request.password());
        return new LoginResponse(token.token(), token.type(), token.expiresAt());
    }
}
