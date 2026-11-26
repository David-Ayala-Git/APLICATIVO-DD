package dd.conexiones.dd_ms_support.web.controller;

import dd.conexiones.dd_ms_support.application.service.TicketService;
import dd.conexiones.dd_ms_support.web.dto.AssignTicketRequest;
import dd.conexiones.dd_ms_support.web.dto.CreateTicketRequest;
import dd.conexiones.dd_ms_support.web.dto.TicketDTO;
import dd.conexiones.mscommon.module.RequiresModule;
import dd.conexiones.mscommon.role.RequiresRole;
import dd.conexiones.mscommon.role.Roles;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO, Roles.CARTERA})
@RequiresModule("support")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<TicketDTO> findAll() {
        return ticketService.findAll();
    }

    @GetMapping("/{id}")
    public TicketDTO findById(@PathVariable UUID id) {
        return ticketService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDTO create(@Valid @RequestBody CreateTicketRequest request) {
        return ticketService.create(request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @PostMapping("/{id}/assign")
    public TicketDTO assign(@PathVariable UUID id, @Valid @RequestBody AssignTicketRequest request) {
        return ticketService.assign(id, request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO})
    @PostMapping("/{id}/start")
    public TicketDTO start(@PathVariable UUID id) {
        return ticketService.start(id);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO})
    @PostMapping("/{id}/finish")
    public TicketDTO finish(@PathVariable UUID id) {
        return ticketService.finish(id);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO})
    @PostMapping("/{id}/resolve")
    public TicketDTO resolve(@PathVariable UUID id) {
        return ticketService.resolve(id);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @PostMapping("/{id}/close")
    public TicketDTO close(@PathVariable UUID id) {
        return ticketService.close(id);
    }
}
