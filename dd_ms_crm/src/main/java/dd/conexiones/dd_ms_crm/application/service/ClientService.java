package dd.conexiones.dd_ms_crm.application.service;

import dd.conexiones.dd_ms_crm.web.dto.ClientDTO;
import dd.conexiones.dd_ms_crm.web.dto.CreateClientRequest;
import dd.conexiones.dd_ms_crm.web.dto.UpdateClientRequest;
import dd.conexiones.dd_ms_crm.web.dto.UpdateClientStatusRequest;

import java.util.List;
import java.util.UUID;

public interface ClientService {
    List<ClientDTO> findAll();
    ClientDTO findById(UUID id);
    String nextCode();
    ClientDTO create(CreateClientRequest request);
    ClientDTO update(UUID id, UpdateClientRequest request);
    ClientDTO updateStatus(UUID id, UpdateClientStatusRequest request);
}
