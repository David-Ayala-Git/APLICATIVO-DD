package dd.conexiones.dd_ms_reports.application.impl;

import dd.conexiones.dd_ms_reports.application.mapper.ExecutiveReportMapper;
import dd.conexiones.dd_ms_reports.application.service.ExecutiveReportService;
import dd.conexiones.dd_ms_reports.domain.model.ExecutiveSnapshot;
import dd.conexiones.dd_ms_reports.domain.repository.ExecutiveSnapshotRepository;
import dd.conexiones.dd_ms_reports.infrastructure.client.CrmReportClient;
import dd.conexiones.dd_ms_reports.infrastructure.client.dto.ClientSummary;
import dd.conexiones.dd_ms_reports.infrastructure.client.dto.PlanSummary;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_reports.web.dto.ExecutiveReportDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Read-model ejecutivo: no posee datos propios, los calcula a partir de
 * ddc_ms_crm (clientes activos y su plan). MRR = suma del precio del plan de
 * cada cliente ACTIVE. ARPU = MRR / clientes activos. Churn = clientes RETIRED
 * sobre el total historico (simplificacion sin tracking de eventos por periodo;
 * un churn periodo-a-periodo real requeriria guardar el estado anterior).
 */
@Service
@Transactional
public class ExecutiveReportServiceImpl implements ExecutiveReportService {

    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ExecutiveSnapshotRepository executiveSnapshotRepository;
    private final ExecutiveReportMapper executiveReportMapper;
    private final TenantFilterEnabler tenantFilterEnabler;
    private final CrmReportClient crmReportClient;

    @PersistenceContext
    private EntityManager entityManager;

    public ExecutiveReportServiceImpl(ExecutiveSnapshotRepository executiveSnapshotRepository,
                                       ExecutiveReportMapper executiveReportMapper,
                                       TenantFilterEnabler tenantFilterEnabler,
                                       CrmReportClient crmReportClient) {
        this.executiveSnapshotRepository = executiveSnapshotRepository;
        this.executiveReportMapper = executiveReportMapper;
        this.tenantFilterEnabler = tenantFilterEnabler;
        this.crmReportClient = crmReportClient;
    }

    @Override
    public ExecutiveReportDTO getCurrent() {
        String tenantId = TenantContext.require();
        String period = YearMonth.now().format(PERIOD_FORMAT);

        tenantFilterEnabler.enable(entityManager);
        return executiveSnapshotRepository.findByTenantIdAndPeriod(tenantId, period)
                .map(executiveReportMapper::toDto)
                .orElseGet(this::refresh);
    }

    @Override
    public ExecutiveReportDTO refresh() {
        String tenantId = TenantContext.require();
        String period = YearMonth.now().format(PERIOD_FORMAT);

        List<ClientSummary> clients = crmReportClient.findClients(tenantId);
        Map<UUID, BigDecimal> pricesByPlan = crmReportClient.findPlans(tenantId).stream()
                .collect(Collectors.toMap(PlanSummary::id, PlanSummary::price));

        int totalClients = clients.size();
        List<ClientSummary> activeClients = clients.stream()
                .filter(c -> "ACTIVE".equals(c.status()))
                .toList();
        long retiredClients = clients.stream().filter(c -> "RETIRED".equals(c.status())).count();

        BigDecimal mrr = activeClients.stream()
                .map(c -> pricesByPlan.getOrDefault(c.planId(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal arpu = activeClients.isEmpty()
                ? BigDecimal.ZERO
                : mrr.divide(BigDecimal.valueOf(activeClients.size()), MathContext.DECIMAL64)
                     .setScale(2, RoundingMode.HALF_UP);

        BigDecimal churnRate = totalClients == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(retiredClients)
                        .divide(BigDecimal.valueOf(totalClients), MathContext.DECIMAL64)
                        .setScale(4, RoundingMode.HALF_UP);

        tenantFilterEnabler.enable(entityManager);
        ExecutiveSnapshot snapshot = executiveSnapshotRepository.findByTenantIdAndPeriod(tenantId, period)
                .orElseGet(ExecutiveSnapshot::new);
        snapshot.setTenantId(tenantId);
        snapshot.setPeriod(period);
        snapshot.setMrr(mrr.setScale(2, RoundingMode.HALF_UP));
        snapshot.setArpu(arpu);
        snapshot.setChurnRate(churnRate);
        snapshot.setActiveClients(activeClients.size());
        snapshot.setRetiredClients((int) retiredClients);
        snapshot.setTotalClients(totalClients);
        snapshot.setGeneratedAt(java.time.OffsetDateTime.now());

        return executiveReportMapper.toDto(executiveSnapshotRepository.saveAndFlush(snapshot));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutiveReportDTO> history() {
        tenantFilterEnabler.enable(entityManager);
        return executiveSnapshotRepository.findAllByOrderByPeriodDesc().stream()
                .map(executiveReportMapper::toDto)
                .toList();
    }
}
