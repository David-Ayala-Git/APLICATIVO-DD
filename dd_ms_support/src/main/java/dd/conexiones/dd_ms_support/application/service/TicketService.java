package dd.conexiones.dd_ms_support.application.service;

import dd.conexiones.dd_ms_support.web.dto.AssignTicketRequest;
import dd.conexiones.dd_ms_support.web.dto.CreateTicketRequest;
import dd.conexiones.dd_ms_support.web.dto.TicketDTO;

import java.util.List;
import java.util.UUID;

public interface TicketService {
    List<TicketDTO> findAll();
    TicketDTO findById(UUID id);
    TicketDTO create(CreateTicketRequest request);
    TicketDTO assign(UUID id, AssignTicketRequest request);
    TicketDTO resolve(UUID id);
    TicketDTO close(UUID id);
}
