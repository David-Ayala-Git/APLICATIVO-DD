package dd.conexiones.dd_ms_reports.web.controller;

import dd.conexiones.dd_ms_reports.application.service.ExecutiveReportService;
import dd.conexiones.dd_ms_reports.web.dto.ExecutiveReportDTO;
import dd.conexiones.mscommon.module.RequiresModule;
import dd.conexiones.mscommon.role.RequiresRole;
import dd.conexiones.mscommon.role.Roles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiresModule("reports")
@RequiresRole({Roles.ADMIN_ISP, Roles.CARTERA})
public class ExecutiveReportController {

    private final ExecutiveReportService executiveReportService;

    public ExecutiveReportController(ExecutiveReportService executiveReportService) {
        this.executiveReportService = executiveReportService;
    }

    @GetMapping("/executive")
    public ExecutiveReportDTO getCurrent() {
        return executiveReportService.getCurrent();
    }

    @RequiresRole({Roles.ADMIN_ISP})
    @PostMapping("/executive/refresh")
    public ExecutiveReportDTO refresh() {
        return executiveReportService.refresh();
    }

    @GetMapping("/executive/history")
    public List<ExecutiveReportDTO> history() {
        return executiveReportService.history();
    }
}
