package dd.conexiones.dd_ms_comms.infrastructure.client;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Definicion de las tools que un agente puede invocar, siguiendo el patron de la guia. */
@Component
public class AiToolDefinitions {

    public List<Tool> all() {
        return List.of(consultarCliente(), crearTicket(), estadoFactura());
    }

    private Tool consultarCliente() {
        return Tool.builder()
                .name("consultar_cliente")
                .description("Consulta los datos y el estado de un cliente del ISP por su identificador")
                .inputSchema(Tool.InputSchema.builder()
                        .properties(Tool.InputSchema.Properties.builder()
                                .putAdditionalProperty("clientId", JsonValue.from(Map.of(
                                        "type", "string",
                                        "description", "UUID del cliente en ddc_ms_crm")))
                                .build())
                        .required(List.of("clientId"))
                        .build())
                .build();
    }

    private Tool crearTicket() {
        return Tool.builder()
                .name("crear_ticket")
                .description("Crea un ticket de soporte para un cliente")
                .inputSchema(Tool.InputSchema.builder()
                        .properties(Tool.InputSchema.Properties.builder()
                                .putAdditionalProperty("clientId", JsonValue.from(Map.of(
                                        "type", "string", "description", "UUID del cliente")))
                                .putAdditionalProperty("reason", JsonValue.from(Map.of(
                                        "type", "string", "description", "Motivo del ticket")))
                                .putAdditionalProperty("priority", JsonValue.from(Map.of(
                                        "type", "string",
                                        "enum", List.of("LOW", "MEDIUM", "HIGH", "URGENT"))))
                                .build())
                        .required(List.of("clientId", "reason", "priority"))
                        .build())
                .build();
    }

    private Tool estadoFactura() {
        return Tool.builder()
                .name("estado_factura")
                .description("Consulta el estado de una factura (pendiente, pagada, vencida)")
                .inputSchema(Tool.InputSchema.builder()
                        .properties(Tool.InputSchema.Properties.builder()
                                .putAdditionalProperty("invoiceId", JsonValue.from(Map.of(
                                        "type", "string", "description", "UUID de la factura en ddc_ms_billing")))
                                .build())
                        .required(List.of("invoiceId"))
                        .build())
                .build();
    }
}
