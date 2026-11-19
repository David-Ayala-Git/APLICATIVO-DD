package dd.conexiones.dd_ms_crm.application.service;

import dd.conexiones.dd_ms_crm.web.dto.ContractDTO;
import dd.conexiones.dd_ms_crm.web.dto.CreateContractRequest;

import java.util.List;
import java.util.UUID;

public interface ContractService {
    List<ContractDTO> findAll();
    ContractDTO findById(UUID id);
    ContractDTO create(CreateContractRequest request);
    ContractDTO sign(UUID id);
}
