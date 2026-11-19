package dd.conexiones.dd_ms_network.application.impl;

import dd.conexiones.dd_ms_network.application.mapper.RouterMapper;
import dd.conexiones.dd_ms_network.application.service.RouterService;
import dd.conexiones.dd_ms_network.domain.exception.RouterNotFoundException;
import dd.conexiones.dd_ms_network.domain.model.Router;
import dd.conexiones.dd_ms_network.domain.model.enums.RouterStatus;
import dd.conexiones.dd_ms_network.domain.repository.RouterRepository;
import dd.conexiones.dd_ms_network.infrastructure.client.MikroTikClient;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_network.infrastructure.security.CryptoService;
import dd.conexiones.dd_ms_network.web.dto.CreateRouterRequest;
import dd.conexiones.dd_ms_network.web.dto.CutClientRequest;
import dd.conexiones.dd_ms_network.web.dto.QueueLimitRequest;
import dd.conexiones.dd_ms_network.web.dto.RouterDTO;
import dd.conexiones.mscommon.audit.Audited;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RouterServiceImpl implements RouterService {

    private final RouterRepository routerRepository;
    private final RouterMapper routerMapper;
    private final TenantFilterEnabler tenantFilterEnabler;
    private final CryptoService cryptoService;
    private final MikroTikClient mikroTikClient;

    @PersistenceContext
    private EntityManager entityManager;

    public RouterServiceImpl(RouterRepository routerRepository, RouterMapper routerMapper,
                              TenantFilterEnabler tenantFilterEnabler, CryptoService cryptoService,
                              MikroTikClient mikroTikClient) {
        this.routerRepository = routerRepository;
        this.routerMapper = routerMapper;
        this.tenantFilterEnabler = tenantFilterEnabler;
        this.cryptoService = cryptoService;
        this.mikroTikClient = mikroTikClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouterDTO> findAll() {
        tenantFilterEnabler.enable(entityManager);
        return routerRepository.findAll().stream().map(routerMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RouterDTO findById(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        return routerRepository.findById(id).map(routerMapper::toDto)
                .orElseThrow(() -> new RouterNotFoundException(id.toString()));
    }

    @Override
    public RouterDTO create(CreateRouterRequest request) {
        Router router = new Router();
        router.setTenantId(TenantContext.require());
        router.setName(request.name());
        router.setHost(request.host());
        router.setApiPort(request.apiPort());
        router.setApiUser(request.apiUser());
        router.setApiPasswordEnc(cryptoService.encrypt(request.apiPassword()));
        router.setModel(request.model());
        router.setStatus(RouterStatus.OFFLINE);

        return routerMapper.toDto(routerRepository.saveAndFlush(router));
    }

    @Override
    @Audited("router.cut")
    public void cutClient(UUID routerId, CutClientRequest request) {
        Router router = getRouterOrThrow(routerId);
        mikroTikClient.disableSecret(router, request.secretId());
    }

    @Override
    @Audited("router.enable")
    public void enableClient(UUID routerId, CutClientRequest request) {
        Router router = getRouterOrThrow(routerId);
        mikroTikClient.enableSecret(router, request.secretId());
    }

    @Override
    @Audited("router.queue")
    public void setQueueLimit(UUID routerId, QueueLimitRequest request) {
        Router router = getRouterOrThrow(routerId);
        mikroTikClient.setQueueLimit(router, request.queueId(), request.uploadKbps(), request.downloadKbps());
    }

    private Router getRouterOrThrow(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        return routerRepository.findById(id).orElseThrow(() -> new RouterNotFoundException(id.toString()));
    }
}
