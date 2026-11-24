package dd.conexiones.dd_ms_auth.web.exception;

import dd.conexiones.dd_ms_auth.domain.exception.CustomRoleNotFoundException;
import dd.conexiones.dd_ms_auth.domain.exception.DuplicateEmailException;
import dd.conexiones.dd_ms_auth.domain.exception.DuplicateRoleNameException;
import dd.conexiones.dd_ms_auth.domain.exception.DuplicateSlugException;
import dd.conexiones.dd_ms_auth.domain.exception.InsufficientRoleException;
import dd.conexiones.dd_ms_auth.domain.exception.InvalidCredentialsException;
import dd.conexiones.dd_ms_auth.domain.exception.InvalidUserTenantException;
import dd.conexiones.dd_ms_auth.domain.exception.RoleInUseException;
import dd.conexiones.dd_ms_auth.domain.exception.TenantNotFoundException;
import dd.conexiones.dd_ms_auth.domain.exception.TooManyLoginAttemptsException;
import dd.conexiones.dd_ms_auth.domain.exception.UserNotFoundException;
import dd.conexiones.dd_ms_auth.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "dd.conexiones.dd_ms_auth")
public class GlobalExceptionHandler {

    @ExceptionHandler({TenantNotFoundException.class, UserNotFoundException.class, CustomRoleNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({DuplicateSlugException.class, DuplicateEmailException.class,
            DuplicateRoleNameException.class, RoleInUseException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(InvalidCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(TooManyLoginAttemptsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyAttempts(TooManyLoginAttemptsException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
                .body(body);
    }

    @ExceptionHandler(InsufficientRoleException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(InsufficientRoleException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidUserTenantException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserTenant(InvalidUserTenantException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Solicitud invalida");
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
