package dd.conexiones.dd_ms_crm.application.impl;

import dd.conexiones.dd_ms_crm.application.mapper.ContractMapper;
import dd.conexiones.dd_ms_crm.application.service.ContractService;
import dd.conexiones.dd_ms_crm.application.validation.ContractValidator;
import dd.conexiones.dd_ms_crm.domain.exception.ContractNotFoundException;
import dd.conexiones.dd_ms_crm.domain.model.Contract;
import dd.conexiones.dd_ms_crm.domain.model.enums.ContractStatus;
import dd.conexiones.dd_ms_crm.domain.repository.ContractRepository;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_crm.web.dto.ContractDTO;
import dd.conexiones.dd_ms_crm.web.dto.CreateContractRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ContractValidator contractValidator;
    private final ContractMapper contractMapper;
    private final TenantFilterEnabler tenantFilterEnabler;

    @PersistenceContext
    private EntityManager entityManager;

    public ContractServiceImpl(ContractRepository contractRepository, ContractValidator contractValidator,
                                ContractMapper contractMapper, TenantFilterEnabler tenantFilterEnabler) {
        this.contractRepository = contractRepository;
        this.contractValidator = contractValidator;
        this.contractMapper = contractMapper;
        this.tenantFilterEnabler = tenantFilterEnabler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDTO> findAll() {
        tenantFilterEnabler.enable(entityManager);
        return contractRepository.findAll().stream().map(contractMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDTO findById(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        return contractRepository.findById(id).map(contractMapper::toDto)
                .orElseThrow(() -> new ContractNotFoundException(id.toString()));
    }

    @Override
    public ContractDTO create(CreateContractRequest request) {
        contractValidator.validateClientExists(request.clientId());
        contractValidator.validatePlanExists(request.planId());

        Contract contract = new Contract();
        contract.setTenantId(TenantContext.require());
        contract.setClientId(request.clientId());
        contract.setPlanId(request.planId());
        contract.setDocumentUrl(request.documentUrl());

        return contractMapper.toDto(contractRepository.saveAndFlush(contract));
    }

    @Override
    public ContractDTO sign(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException(id.toString()));

        contract.setStatus(ContractStatus.SIGNED);
        contract.setSignedAt(OffsetDateTime.now());

        return contractMapper.toDto(contractRepository.save(contract));
    }
}
