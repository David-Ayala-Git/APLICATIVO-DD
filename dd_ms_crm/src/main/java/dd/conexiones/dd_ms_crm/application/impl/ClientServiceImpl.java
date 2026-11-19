package dd.conexiones.dd_ms_crm.application.impl;

import dd.conexiones.dd_ms_crm.application.mapper.ClientMapper;
import dd.conexiones.dd_ms_crm.application.service.ClientService;
import dd.conexiones.dd_ms_crm.application.validation.ClientValidator;
import dd.conexiones.dd_ms_crm.domain.exception.ClientNotFoundException;
import dd.conexiones.dd_ms_crm.domain.model.Client;
import dd.conexiones.dd_ms_crm.domain.repository.ClientRepository;
import dd.conexiones.mscommon.audit.Audited;
import dd.conexiones.mscommon.automation.AutomationEvents;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_crm.web.dto.ClientDTO;
import dd.conexiones.dd_ms_crm.web.dto.CreateClientRequest;
import dd.conexiones.dd_ms_crm.web.dto.UpdateClientRequest;
import dd.conexiones.dd_ms_crm.web.dto.UpdateClientStatusRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientValidator clientValidator;
    private final ClientMapper clientMapper;
    private final TenantFilterEnabler tenantFilterEnabler;
    private final AutomationEvents automationEvents;

    @PersistenceContext
    private EntityManager entityManager;

    public ClientServiceImpl(ClientRepository clientRepository, ClientValidator clientValidator,
                              ClientMapper clientMapper, TenantFilterEnabler tenantFilterEnabler,
                              AutomationEvents automationEvents) {
        this.clientRepository = clientRepository;
        this.clientValidator = clientValidator;
        this.clientMapper = clientMapper;
        this.tenantFilterEnabler = tenantFilterEnabler;
        this.automationEvents = automationEvents;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDTO> findAll() {
        tenantFilterEnabler.enable(entityManager);
        return clientRepository.findAll().stream().map(clientMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDTO findById(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        return findOwnClient(id).map(clientMapper::toDto)
                .orElseThrow(() -> new ClientNotFoundException(id.toString()));
    }

    @Override
    public ClientDTO create(CreateClientRequest request) {
        String tenantId = TenantContext.require();
        clientValidator.validateDocumentAvailable(tenantId, request.document());

        Client client = new Client();
        client.setTenantId(tenantId);
        client.setFullName(request.fullName());
        client.setDocument(request.document());
        client.setPhone(request.phone());
        client.setAddress(request.address());
        client.setPlanId(request.planId());

        client = clientRepository.saveAndFlush(client);
        // n8n (flujo de onboarding): orden de instalacion + bienvenida por WhatsApp
        automationEvents.publish("client-created", Map.of(
                "clientId", client.getId().toString(),
                "fullName", client.getFullName(),
                "phone", client.getPhone() == null ? "" : client.getPhone(),
                "address", client.getAddress() == null ? "" : client.getAddress()));
        return clientMapper.toDto(client);
    }

    @Override
    @Audited("client.update")
    public ClientDTO update(UUID id, UpdateClientRequest request) {
        Client client = findOwnClient(id)
                .orElseThrow(() -> new ClientNotFoundException(id.toString()));
        client.setFullName(request.fullName());
        client.setPhone(request.phone());
        client.setAddress(request.address());
        client.setPlanId(request.planId());
        client.setStatus(request.status());
        client.setIpAddress(request.ipAddress());
        client.setOnuSerial(request.onuSerial());
        client.setRouterId(request.routerId());
        client.setPppoeUser(request.pppoeUser());
        return clientMapper.toDto(clientRepository.save(client));
    }

    @Override
    @Audited("client.status")
    public ClientDTO updateStatus(UUID id, UpdateClientStatusRequest request) {
        Client client = findOwnClient(id)
                .orElseThrow(() -> new ClientNotFoundException(id.toString()));
        client.setStatus(request.status());
        return clientMapper.toDto(clientRepository.save(client));
    }

    /** El filtro de Hibernate no aplica a una carga por id: se compara el tenant a mano. */
    private Optional<Client> findOwnClient(UUID id) {
        return clientRepository.findById(id)
                .filter(client -> client.getTenantId().equals(TenantContext.require()));
    }
}
