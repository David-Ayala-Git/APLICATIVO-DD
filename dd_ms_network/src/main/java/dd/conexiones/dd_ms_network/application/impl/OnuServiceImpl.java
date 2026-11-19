package dd.conexiones.dd_ms_network.application.impl;

import dd.conexiones.dd_ms_network.application.mapper.OnuMapper;
import dd.conexiones.dd_ms_network.application.service.OnuService;
import dd.conexiones.dd_ms_network.application.validation.OnuValidator;
import dd.conexiones.dd_ms_network.domain.exception.OnuNotFoundException;
import dd.conexiones.dd_ms_network.domain.model.Onu;
import dd.conexiones.dd_ms_network.domain.model.enums.OnuStatus;
import dd.conexiones.dd_ms_network.domain.repository.OnuRepository;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_network.web.dto.AuthorizeOnuRequest;
import dd.conexiones.dd_ms_network.web.dto.OnuDTO;
import dd.conexiones.dd_ms_network.web.dto.RegisterOnuRequest;
import dd.conexiones.dd_ms_network.web.dto.SignalUpdateRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Autorizacion y monitoreo de ONU. La lectura de Rx power / estado optico real
 * (SNMP/NETCONF/Telnet contra la OLT) no esta implementada: no hay una OLT real
 * disponible en este entorno. updateSignal() deja el punto de integracion listo
 * para que un poller (o webhook del NMS del fabricante) reporte la lectura.
 */
@Service
@Transactional
public class OnuServiceImpl implements OnuService {

    private final OnuRepository onuRepository;
    private final OnuValidator onuValidator;
    private final OnuMapper onuMapper;
    private final TenantFilterEnabler tenantFilterEnabler;

    @PersistenceContext
    private EntityManager entityManager;

    public OnuServiceImpl(OnuRepository onuRepository, OnuValidator onuValidator, OnuMapper onuMapper,
                           TenantFilterEnabler tenantFilterEnabler) {
        this.onuRepository = onuRepository;
        this.onuValidator = onuValidator;
        this.onuMapper = onuMapper;
        this.tenantFilterEnabler = tenantFilterEnabler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OnuDTO> findAll() {
        tenantFilterEnabler.enable(entityManager);
        return onuRepository.findAll().stream().map(onuMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OnuDTO findById(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        return onuRepository.findById(id).map(onuMapper::toDto)
                .orElseThrow(() -> new OnuNotFoundException(id.toString()));
    }

    @Override
    public OnuDTO register(RegisterOnuRequest request) {
        String tenantId = TenantContext.require();
        onuValidator.validateSerialAvailable(tenantId, request.serial());

        Onu onu = new Onu();
        onu.setTenantId(tenantId);
        onu.setSerial(request.serial());
        onu.setVendor(request.vendor());
        onu.setOltName(request.oltName());
        onu.setPonPort(request.ponPort());
        onu.setStatus(OnuStatus.UNAUTHORIZED);

        return onuMapper.toDto(onuRepository.saveAndFlush(onu));
    }

    @Override
    public OnuDTO authorize(UUID id, AuthorizeOnuRequest request) {
        tenantFilterEnabler.enable(entityManager);
        Onu onu = onuRepository.findById(id).orElseThrow(() -> new OnuNotFoundException(id.toString()));

        onu.setClientId(request.clientId());
        onu.setStatus(OnuStatus.ONLINE);

        return onuMapper.toDto(onuRepository.save(onu));
    }

    @Override
    public OnuDTO updateSignal(UUID id, SignalUpdateRequest request) {
        tenantFilterEnabler.enable(entityManager);
        Onu onu = onuRepository.findById(id).orElseThrow(() -> new OnuNotFoundException(id.toString()));

        onu.setRxPower(request.rxPower());
        // Umbral de la guia: por debajo de -27 dBm se considera senal critica (LOS).
        if (onu.getStatus() != OnuStatus.UNAUTHORIZED) {
            onu.setStatus(request.rxPower() < -27.0 ? OnuStatus.LOS : OnuStatus.ONLINE);
        }

        return onuMapper.toDto(onuRepository.save(onu));
    }
}
