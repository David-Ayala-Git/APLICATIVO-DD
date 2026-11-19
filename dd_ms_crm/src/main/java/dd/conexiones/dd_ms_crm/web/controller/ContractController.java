package dd.conexiones.dd_ms_crm.web.controller;

import dd.conexiones.dd_ms_crm.application.service.ContractService;
import dd.conexiones.dd_ms_crm.web.dto.ContractDTO;
import dd.conexiones.dd_ms_crm.web.dto.CreateContractRequest;
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
@RequestMapping("/api/contracts")
@RequiresModule("contracts")
@RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.CARTERA})
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    public List<ContractDTO> findAll() {
        return contractService.findAll();
    }

    @GetMapping("/{id}")
    public ContractDTO findById(@PathVariable UUID id) {
        return contractService.findById(id);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContractDTO create(@Valid @RequestBody CreateContractRequest request) {
        return contractService.create(request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @PostMapping("/{id}/sign")
    public ContractDTO sign(@PathVariable UUID id) {
        return contractService.sign(id);
    }
}
