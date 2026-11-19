package dd.conexiones.dd_ms_auth.application.impl;

import dd.conexiones.dd_ms_auth.application.mapper.TenantMapper;
import dd.conexiones.dd_ms_auth.application.service.TenantService;
import dd.conexiones.dd_ms_auth.application.validation.CallerAccess;
import dd.conexiones.dd_ms_auth.application.validation.TenantValidator;
import dd.conexiones.dd_ms_auth.domain.exception.TenantNotFoundException;
import dd.conexiones.dd_ms_auth.domain.model.Tenant;
import dd.conexiones.dd_ms_auth.domain.model.TenantModule;
import dd.conexiones.dd_ms_auth.domain.model.TenantModuleId;
import dd.conexiones.dd_ms_auth.domain.repository.TenantModuleRepository;
import dd.conexiones.dd_ms_auth.domain.repository.TenantRepository;
import dd.conexiones.dd_ms_auth.web.dto.CreateTenantRequest;
import dd.conexiones.dd_ms_auth.web.dto.ModuleStatusDTO;
import dd.conexiones.dd_ms_auth.web.dto.TenantDTO;
import dd.conexiones.dd_ms_auth.web.dto.UpdateTenantBrandingRequest;
import dd.conexiones.dd_ms_auth.web.dto.UpdateTenantModuleRequest;
import dd.conexiones.mscommon.audit.Audited;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TenantServiceImpl implements TenantService {

    /** Mismos modulos que habilita V5__seed_core_modules.sql para los tenants existentes. */
    private static final List<String> CORE_MODULES = List.of("crm", "billing", "contracts");

    private final TenantRepository tenantRepository;
    private final TenantModuleRepository tenantModuleRepository;
    private final TenantValidator tenantValidator;
    private final TenantMapper tenantMapper;
    private final CallerAccess callerAccess;

    public TenantServiceImpl(TenantRepository tenantRepository, TenantModuleRepository tenantModuleRepository,
                              TenantValidator tenantValidator, TenantMapper tenantMapper,
                              CallerAccess callerAccess) {
        this.tenantRepository = tenantRepository;
        this.tenantModuleRepository = tenantModuleRepository;
        this.tenantValidator = tenantValidator;
        this.tenantMapper = tenantMapper;
        this.callerAccess = callerAccess;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantDTO> findAll() {
        // el resto de roles solo ve su propio proveedor (el front lo necesita
        // para el sidebar y los guards de modulo)
        List<Tenant> visible = callerAccess.isSuperAdmin()
                ? tenantRepository.findAll()
                : callerAccess.callerTenant().map(List::of).orElse(List.of());
        return visible.stream()
                .map(tenant -> tenantMapper.toDto(tenant, tenantModuleRepository.findById_TenantId(tenant.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TenantDTO findById(UUID id) {
        Tenant tenant = getTenantOrThrow(id);
        if (!callerAccess.isSuperAdmin() && !callerAccess.ownsTenant(tenant)) {
            // 404 y no 403: no confirmar que existe un proveedor ajeno
            throw new TenantNotFoundException(id.toString());
        }
        return tenantMapper.toDto(tenant, tenantModuleRepository.findById_TenantId(id));
    }

    @Override
    public TenantDTO create(CreateTenantRequest request) {
        callerAccess.requireSuperAdmin();
        tenantValidator.validateSlugAvailable(request.slug());

        Tenant tenant = new Tenant();
        tenant.setName(request.name());
        tenant.setSlug(request.slug());
        tenant = tenantRepository.saveAndFlush(tenant);

        // Nucleo activo desde el alta: sin fila en tenant_module, @RequiresModule
        // lo trata como apagado y el proveedor nuevo no podria ni crear clientes.
        UUID tenantId = tenant.getId();
        List<TenantModule> core = CORE_MODULES.stream().map(key -> {
            TenantModule module = new TenantModule();
            module.setId(new TenantModuleId(tenantId, key));
            module.setEnabled(true);
            return module;
        }).toList();
        tenantModuleRepository.saveAll(core);

        return tenantMapper.toDto(tenant, core);
    }

    @Override
    @Audited("tenant.module_toggle")
    public TenantDTO updateModule(UUID tenantId, String moduleKey, UpdateTenantModuleRequest request) {
        callerAccess.requireTenantAdmin(getTenantOrThrow(tenantId));

        TenantModuleId id = new TenantModuleId(tenantId, moduleKey);
        TenantModule module = tenantModuleRepository.findById(id).orElseGet(() -> {
            TenantModule created = new TenantModule();
            created.setId(id);
            return created;
        });
        module.setEnabled(request.enabled());
        module.setConfig(request.config());
        tenantModuleRepository.save(module);

        Tenant tenant = getTenantOrThrow(tenantId);
        return tenantMapper.toDto(tenant, tenantModuleRepository.findById_TenantId(tenantId));
    }

    @Override
    @Audited("tenant.branding")
    public TenantDTO updateBranding(UUID tenantId, UpdateTenantBrandingRequest request) {
        Tenant tenant = getTenantOrThrow(tenantId);
        callerAccess.requireTenantAdmin(tenant);
        // en minusculas: el front compara contra los colores por defecto para no
        // tocar el tema cuando el proveedor no los personalizo
        tenant.setBrandPrimary(request.brandPrimary().toLowerCase());
        tenant.setBrandAccent(request.brandAccent().toLowerCase());
        tenantRepository.save(tenant);
        return tenantMapper.toDto(tenant, tenantModuleRepository.findById_TenantId(tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleStatusDTO checkModule(String slug, String moduleKey) {
        Tenant tenant = tenantRepository.findBySlug(slug).orElseThrow(() -> new TenantNotFoundException(slug));
        boolean enabled = tenantModuleRepository.findById(new TenantModuleId(tenant.getId(), moduleKey))
                .map(TenantModule::isEnabled)
                .orElse(false);
        return new ModuleStatusDTO(moduleKey, enabled);
    }

    private Tenant getTenantOrThrow(UUID id) {
        return tenantRepository.findById(id).orElseThrow(() -> new TenantNotFoundException(id.toString()));
    }
}
