package dd.conexiones.dd_ms_support.application.mapper;

import dd.conexiones.dd_ms_support.domain.model.Technician;
import dd.conexiones.dd_ms_support.web.dto.TechnicianDTO;
import org.springframework.stereotype.Component;

@Component
public class TechnicianMapper {

    public TechnicianDTO toDto(Technician technician) {
        return new TechnicianDTO(technician.getId(), technician.getFullName(), technician.getPhone(),
                technician.getStatus(), technician.getCreatedAt());
    }
}
