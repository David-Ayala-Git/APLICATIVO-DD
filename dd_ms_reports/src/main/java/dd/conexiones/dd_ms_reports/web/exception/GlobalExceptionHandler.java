package dd.conexiones.dd_ms_reports.web.exception;

import dd.conexiones.dd_ms_reports.domain.exception.SnapshotNotFoundException;
import dd.conexiones.dd_ms_reports.web.dto.ErrorResponse;
import dd.conexiones.mscommon.module.ModuleDisabledException;
import dd.conexiones.mscommon.role.RoleNotAllowedException;
import dd.conexiones.mscommon.tenant.CrossTenantAccessException;
import dd.conexiones.mscommon.tenant.MissingTenantException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice(basePackages = "dd.conexiones.dd_ms_reports")
public class GlobalExceptionHandler {

    @ExceptionHandler(SnapshotNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(SnapshotNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MissingTenantException.class)
    public ResponseEntity<ErrorResponse> handleMissingTenant(MissingTenantException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(CrossTenantAccessException.class)
    public ResponseEntity<ErrorResponse> handleCrossTenant(CrossTenantAccessException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ModuleDisabledException.class)
    public ResponseEntity<ErrorResponse> handleModuleDisabled(ModuleDisabledException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(RoleNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotAllowed(RoleNotAllowedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleUpstream(RestClientException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "Error consultando ddc_ms_crm: " + ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
