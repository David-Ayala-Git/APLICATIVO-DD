package dd.conexiones.dd_ms_auth.application.mapper;

import dd.conexiones.dd_ms_auth.domain.model.Tenant;
import dd.conexiones.dd_ms_auth.domain.model.TenantModule;
import dd.conexiones.dd_ms_auth.web.dto.TenantDTO;
import dd.conexiones.dd_ms_auth.web.dto.TenantModuleDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TenantMapper {

    public TenantModuleDTO toDto(TenantModule module) {
        return new TenantModuleDTO(module.getId().getModuleKey(), module.isEnabled(), module.getConfig());
    }

    public TenantDTO toDto(Tenant tenant, List<TenantModule> modules) {
        List<TenantModuleDTO> moduleDtos = modules.stream().map(this::toDto).toList();
        return new TenantDTO(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getPlan(),
                tenant.getStatus(),
                tenant.getBrandPrimary(),
                tenant.getBrandAccent(),
                tenant.getCreatedAt(),
                moduleDtos
        );
    }
}
