package dd.conexiones.dd_ms_crm.application.mapper;

import dd.conexiones.dd_ms_crm.domain.model.Client;
import dd.conexiones.dd_ms_crm.web.dto.ClientDTO;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public ClientDTO toDto(Client client) {
        return new ClientDTO(client.getId(), client.getFullName(), client.getDocument(), client.getPhone(),
                client.getAddress(), client.getPlanId(), client.getStatus(), client.getIpAddress(),
                client.getOnuSerial(), client.getRouterId(), client.getPppoeUser(), client.getCreatedAt());
    }
}
