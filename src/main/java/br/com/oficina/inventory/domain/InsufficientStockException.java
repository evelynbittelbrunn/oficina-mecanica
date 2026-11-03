package br.com.oficina.inventory.domain;

import br.com.oficina.shared.domain.exception.BusinessRuleException;

public class InsufficientStockException extends BusinessRuleException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
