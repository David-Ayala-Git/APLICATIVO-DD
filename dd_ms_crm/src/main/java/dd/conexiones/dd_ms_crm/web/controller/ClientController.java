package dd.conexiones.dd_ms_crm.web.controller;

import dd.conexiones.dd_ms_crm.application.service.ClientService;
import dd.conexiones.dd_ms_crm.web.dto.ClientDTO;
import dd.conexiones.dd_ms_crm.web.dto.CreateClientRequest;
import dd.conexiones.dd_ms_crm.web.dto.UpdateClientRequest;
import dd.conexiones.dd_ms_crm.web.dto.UpdateClientStatusRequest;
import dd.conexiones.mscommon.module.RequiresModule;
import dd.conexiones.mscommon.role.RequiresRole;
import dd.conexiones.mscommon.role.Roles;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/crm/clients")
@RequiresModule("crm")
@RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.CARTERA, Roles.TECNICO})
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<ClientDTO> findAll() {
        return clientService.findAll();
    }

    @GetMapping("/next-code")
    public Map<String, String> nextCode() {
        return Map.of("nextCode", clientService.nextCode());
    }

    @GetMapping("/{id}")
    public ClientDTO findById(@PathVariable UUID id) {
        return clientService.findById(id);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientDTO create(@Valid @RequestBody CreateClientRequest request) {
        return clientService.create(request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @PutMapping("/{id}")
    public ClientDTO update(@PathVariable UUID id, @Valid @RequestBody UpdateClientRequest request) {
        return clientService.update(id, request);
    }

    /** Suspender / reactivar sin reenviar la ficha. Cartera tambien: es quien corta por mora. */
    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.CARTERA})
    @PostMapping("/{id}/status")
    public ClientDTO updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateClientStatusRequest request) {
        return clientService.updateStatus(id, request);
    }
}
