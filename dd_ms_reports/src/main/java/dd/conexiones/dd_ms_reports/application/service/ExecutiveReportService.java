package dd.conexiones.dd_ms_reports.application.service;

import dd.conexiones.dd_ms_reports.web.dto.ExecutiveReportDTO;

import java.util.List;

public interface ExecutiveReportService {
    ExecutiveReportDTO getCurrent();
    ExecutiveReportDTO refresh();
    List<ExecutiveReportDTO> history();
}
