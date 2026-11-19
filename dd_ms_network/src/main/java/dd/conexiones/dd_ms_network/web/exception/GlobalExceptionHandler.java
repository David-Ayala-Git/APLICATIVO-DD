package dd.conexiones.dd_ms_network.web.exception;

import dd.conexiones.dd_ms_network.domain.exception.DuplicateSerialException;
import dd.conexiones.dd_ms_network.domain.exception.EquipmentNotFoundException;
import dd.conexiones.dd_ms_network.domain.exception.InvalidRouterOsIdException;
import dd.conexiones.dd_ms_network.domain.exception.NodeNotFoundException;
import dd.conexiones.dd_ms_network.domain.exception.OnuNotFoundException;
import dd.conexiones.dd_ms_network.domain.exception.RouterConnectionException;
import dd.conexiones.dd_ms_network.domain.exception.RouterNotFoundException;
import dd.conexiones.dd_ms_network.web.dto.ErrorResponse;
import dd.conexiones.mscommon.module.ModuleDisabledException;
import dd.conexiones.mscommon.role.RoleNotAllowedException;
import dd.conexiones.mscommon.tenant.CrossTenantAccessException;
import dd.conexiones.mscommon.tenant.MissingTenantException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({RouterNotFoundException.class, OnuNotFoundException.class,
            EquipmentNotFoundException.class, NodeNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateSerialException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DuplicateSerialException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidRouterOsIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRouterOsId(InvalidRouterOsIdException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(RouterConnectionException.class)
    public ResponseEntity<ErrorResponse> handleRouterConnection(RouterConnectionException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler(MissingTenantException.class)
    public ResponseEntity<ErrorResponse> handleMissingTenant(MissingTenantException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(CrossTenantAccessException.class)
    public ResponseEntity<ErrorResponse> handleCrossTenant(CrossTenantAccessException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(RoleNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotAllowed(RoleNotAllowedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(ModuleDisabledException.class)
    public ResponseEntity<ErrorResponse> handleModuleDisabled(ModuleDisabledException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
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
