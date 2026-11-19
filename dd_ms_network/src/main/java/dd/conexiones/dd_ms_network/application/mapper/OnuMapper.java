package dd.conexiones.dd_ms_network.application.mapper;

import dd.conexiones.dd_ms_network.domain.model.Onu;
import dd.conexiones.dd_ms_network.web.dto.OnuDTO;
import org.springframework.stereotype.Component;

@Component
public class OnuMapper {

    public OnuDTO toDto(Onu onu) {
        return new OnuDTO(onu.getId(), onu.getSerial(), onu.getVendor(), onu.getOltName(), onu.getPonPort(),
                onu.getRxPower(), onu.getStatus(), onu.getClientId(), onu.getCreatedAt());
    }
}
