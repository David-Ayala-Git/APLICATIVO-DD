package dd.conexiones.dd_ms_crm.web.dto;

import dd.conexiones.dd_ms_crm.domain.model.enums.ClientStatus;
import jakarta.validation.constraints.NotNull;

/** Solo el estado: lo usa n8n al cortar/reactivar sin tener que reenviar toda la ficha. */
public record UpdateClientStatusRequest(@NotNull ClientStatus status) {
}
