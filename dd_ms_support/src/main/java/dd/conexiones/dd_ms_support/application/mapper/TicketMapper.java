package dd.conexiones.dd_ms_support.application.mapper;

import dd.conexiones.dd_ms_support.domain.model.Ticket;
import dd.conexiones.dd_ms_support.web.dto.TicketDTO;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketDTO toDto(Ticket ticket) {
        return new TicketDTO(ticket.getId(), ticket.getClientId(), ticket.getReason(), ticket.getPriority(),
                ticket.getStatus(), ticket.getAssignedTo(), ticket.getSlaDueAt(), ticket.getStartedAt(),
                ticket.getFinishedAt(), ticket.getCreatedAt());
    }
}
