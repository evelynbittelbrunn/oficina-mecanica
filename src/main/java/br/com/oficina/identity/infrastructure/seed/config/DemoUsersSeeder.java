package br.com.oficina.identity.infrastructure.seed.config;

import br.com.oficina.identity.application.UserRegistrationService;
import br.com.oficina.identity.domain.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed.users.enabled", havingValue = "true")
@EnableConfigurationProperties(DemoUsersSeeder.DemoUsers.class)
class DemoUsersSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUsersSeeder.class);

    private final UserRegistrationService userRegistrationService;
    private final DemoUsers users;

    DemoUsersSeeder(UserRegistrationService userRegistrationService, DemoUsers users) {
        this.userRegistrationService = userRegistrationService;
        this.users = users;
    }

    @Override
    public void run(ApplicationArguments args) {
        create("Administrador", users.admin(), Role.ADMIN);
        create("Atendente", users.attendant(), Role.ATTENDANT);
    }

    private void create(String name, Credentials credentials, Role role) {
        if (credentials == null || credentials.password() == null || credentials.password().isBlank()) {
            log.warn("Demo user {} not created: password not configured.", role);
            return;
        }

        if (userRegistrationService.createIfAbsent(name, credentials.email(), credentials.password(), role)) {
            log.info("Demo user {} created: {}", role, credentials.email());
        }
    }

    @ConfigurationProperties(prefix = "app.seed.users")
    record DemoUsers(Credentials admin, Credentials attendant) {
    }

    record Credentials(String email, String password) {
    }
}
