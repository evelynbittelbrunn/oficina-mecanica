package br.com.oficina.shared.domain.exception;

public class UnauthenticatedException extends DomainException {

    public UnauthenticatedException(String message) {
        super(message);
    }
}
