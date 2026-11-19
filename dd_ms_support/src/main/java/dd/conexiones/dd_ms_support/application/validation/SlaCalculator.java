package dd.conexiones.dd_ms_support.application.validation;

import dd.conexiones.dd_ms_support.domain.model.enums.Priority;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/** Calcula el vencimiento de SLA segun la prioridad del ticket. */
@Component
public class SlaCalculator {

    public OffsetDateTime dueAt(Priority priority) {
        OffsetDateTime now = OffsetDateTime.now();
        return switch (priority) {
            case URGENT -> now.plusHours(4);
            case HIGH -> now.plusHours(8);
            case MEDIUM -> now.plusHours(24);
            case LOW -> now.plusHours(72);
        };
    }
}
