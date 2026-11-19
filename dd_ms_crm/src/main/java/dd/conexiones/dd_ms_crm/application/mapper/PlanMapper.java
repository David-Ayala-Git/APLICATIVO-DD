package dd.conexiones.dd_ms_crm.application.mapper;

import dd.conexiones.dd_ms_crm.domain.model.Plan;
import dd.conexiones.dd_ms_crm.web.dto.PlanDTO;
import org.springframework.stereotype.Component;

@Component
public class PlanMapper {

    public PlanDTO toDto(Plan plan) {
        return new PlanDTO(plan.getId(), plan.getName(), plan.getDownMbps(), plan.getUpMbps(),
                plan.getTechnology(), plan.getPrice(), plan.getCreatedAt());
    }
}
