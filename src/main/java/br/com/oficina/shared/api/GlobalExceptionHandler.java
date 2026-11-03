package br.com.oficina.shared.api;

import br.com.oficina.shared.domain.exception.BusinessRuleException;
import br.com.oficina.shared.domain.exception.ConflictException;
import br.com.oficina.shared.domain.exception.InvalidTransitionException;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.exception.UnauthenticatedException;
import br.com.oficina.shared.domain.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String INVALID_DATA = "Dados inválidos";

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidation(ValidationException ex) {
        return Problems.of(HttpStatus.BAD_REQUEST, INVALID_DATA, ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return Problems.of(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        return Problems.of(HttpStatus.CONFLICT, "Conflito", ex.getMessage());
    }

    @ExceptionHandler(InvalidTransitionException.class)
    public ProblemDetail handleInvalidTransition(InvalidTransitionException ex) {
        return Problems.of(HttpStatus.CONFLICT, "Transição de status inválida", ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleConcurrency(OptimisticLockingFailureException ex) {
        return Problems.of(
                HttpStatus.CONFLICT,
                "Conflito de concorrência",
                "O registro foi alterado por outra operação. Consulte novamente e repita a operação."
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return Problems.of(
                HttpStatus.CONFLICT,
                "Conflito",
                "A operação viola uma restrição de integridade (registro duplicado ou ainda referenciado)."
        );
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        return Problems.of(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", ex.getMessage());
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public ProblemDetail handleUnauthenticated(UnauthenticatedException ex) {
        return Problems.of(HttpStatus.UNAUTHORIZED, "Não autenticado", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return Problems.of(HttpStatus.FORBIDDEN, "Acesso negado", "Você não tem permissão para esta operação.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return Problems.of(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = Problems.of(HttpStatus.BAD_REQUEST, INVALID_DATA, "Requisição inválida.");
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return ResponseEntity.badRequest().body(Problems.of(
                HttpStatus.BAD_REQUEST,
                INVALID_DATA,
                "Corpo da requisição inválido: JSON malformado, com tipo de campo incorreto ou fora do padrão UTF-8."));
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(result -> errors.putIfAbsent(
                result.getMethodParameter().getParameterName(),
                result.getResolvableErrors().getFirst().getDefaultMessage()
        ));

        ProblemDetail problem = Problems.of(HttpStatus.BAD_REQUEST, INVALID_DATA, "Requisição inválida.");
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }
}
