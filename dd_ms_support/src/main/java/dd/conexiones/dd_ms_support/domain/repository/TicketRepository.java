package dd.conexiones.dd_ms_support.domain.repository;

import dd.conexiones.dd_ms_support.domain.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
}
