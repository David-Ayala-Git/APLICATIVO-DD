package dd.conexiones.dd_ms_reports.application.mapper;

import dd.conexiones.dd_ms_reports.domain.model.ExecutiveSnapshot;
import dd.conexiones.dd_ms_reports.web.dto.ExecutiveReportDTO;
import org.springframework.stereotype.Component;

@Component
public class ExecutiveReportMapper {

    public ExecutiveReportDTO toDto(ExecutiveSnapshot snapshot) {
        return new ExecutiveReportDTO(snapshot.getPeriod(), snapshot.getMrr(), snapshot.getArpu(),
                snapshot.getChurnRate(), snapshot.getActiveClients(), snapshot.getRetiredClients(),
                snapshot.getTotalClients(), snapshot.getGeneratedAt());
    }
}
