package dd.conexiones.dd_ms_support.application.impl;

import dd.conexiones.dd_ms_support.application.mapper.TicketMapper;
import dd.conexiones.dd_ms_support.application.service.TicketService;
import dd.conexiones.dd_ms_support.application.validation.SlaCalculator;
import dd.conexiones.dd_ms_support.application.validation.TechnicianValidator;
import dd.conexiones.dd_ms_support.domain.exception.TicketNotFoundException;
import dd.conexiones.dd_ms_support.domain.model.Ticket;
import dd.conexiones.dd_ms_support.domain.model.enums.TicketStatus;
import dd.conexiones.dd_ms_support.domain.repository.TicketRepository;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_support.web.dto.AssignTicketRequest;
import dd.conexiones.dd_ms_support.web.dto.CreateTicketRequest;
import dd.conexiones.dd_ms_support.web.dto.TicketDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final SlaCalculator slaCalculator;
    private final TechnicianValidator technicianValidator;
    private final TenantFilterEnabler tenantFilterEnabler;

    @PersistenceContext
    private EntityManager entityManager;

    public TicketServiceImpl(TicketRepository ticketRepository, TicketMapper ticketMapper,
                              SlaCalculator slaCalculator, TechnicianValidator technicianValidator,
                              TenantFilterEnabler tenantFilterEnabler) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.slaCalculator = slaCalculator;
        this.technicianValidator = technicianValidator;
        this.tenantFilterEnabler = tenantFilterEnabler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> findAll() {
        tenantFilterEnabler.enable(entityManager);
        return ticketRepository.findAll().stream().map(ticketMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDTO findById(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        return ticketRepository.findById(id).map(ticketMapper::toDto)
                .orElseThrow(() -> new TicketNotFoundException(id.toString()));
    }

    @Override
    public TicketDTO create(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTenantId(TenantContext.require());
        ticket.setClientId(request.clientId());
        ticket.setReason(request.reason());
        ticket.setPriority(request.priority());
        ticket.setSlaDueAt(slaCalculator.dueAt(request.priority()));

        return ticketMapper.toDto(ticketRepository.saveAndFlush(ticket));
    }

    @Override
    public TicketDTO assign(UUID id, AssignTicketRequest request) {
        technicianValidator.validateExists(request.technicianId());
        Ticket ticket = getTicketOrThrow(id);

        ticket.setAssignedTo(request.technicianId());
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        return ticketMapper.toDto(ticketRepository.save(ticket));
    }

    @Override
    public TicketDTO resolve(UUID id) {
        Ticket ticket = getTicketOrThrow(id);
        ticket.setStatus(TicketStatus.RESOLVED);
        return ticketMapper.toDto(ticketRepository.save(ticket));
    }

    @Override
    public TicketDTO close(UUID id) {
        Ticket ticket = getTicketOrThrow(id);
        ticket.setStatus(TicketStatus.CLOSED);
        return ticketMapper.toDto(ticketRepository.save(ticket));
    }

    private Ticket getTicketOrThrow(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        return ticketRepository.findById(id).orElseThrow(() -> new TicketNotFoundException(id.toString()));
    }
}
