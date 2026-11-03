package br.com.oficina.identity.application;

import br.com.oficina.identity.application.dto.AccessToken;
import br.com.oficina.identity.domain.User;

public interface TokenGenerator {

    AccessToken generate(User user);
}
