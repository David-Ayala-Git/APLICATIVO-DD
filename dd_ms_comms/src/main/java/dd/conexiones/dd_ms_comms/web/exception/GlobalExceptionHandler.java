package dd.conexiones.dd_ms_comms.web.exception;

import dd.conexiones.dd_ms_comms.domain.exception.AgentDisabledException;
import dd.conexiones.dd_ms_comms.domain.exception.AiAgentNotFoundException;
import dd.conexiones.dd_ms_comms.domain.exception.UnroutableWebhookException;
import dd.conexiones.dd_ms_comms.domain.exception.WhatsappSendException;
import dd.conexiones.dd_ms_comms.web.dto.ErrorResponse;
import dd.conexiones.mscommon.module.ModuleDisabledException;
import dd.conexiones.mscommon.role.RoleNotAllowedException;
import dd.conexiones.mscommon.tenant.CrossTenantAccessException;
import dd.conexiones.mscommon.tenant.MissingTenantException;
import com.anthropic.errors.AnthropicException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "dd.conexiones.dd_ms_comms")
public class GlobalExceptionHandler {

    @ExceptionHandler(AiAgentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(AiAgentNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(AgentDisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(AgentDisabledException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(WhatsappSendException.class)
    public ResponseEntity<ErrorResponse> handleWhatsappSend(WhatsappSendException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler(AnthropicException.class)
    public ResponseEntity<ErrorResponse> handleAnthropic(AnthropicException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "Error consultando la API de Anthropic: " + ex.getMessage(), request);
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

    @ExceptionHandler(UnroutableWebhookException.class)
    public ResponseEntity<ErrorResponse> handleUnroutableWebhook(UnroutableWebhookException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
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
