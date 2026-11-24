package dd.conexiones.dd_ms_auth.application.mapper;

import dd.conexiones.dd_ms_auth.domain.model.CustomRole;
import dd.conexiones.dd_ms_auth.web.dto.CustomRoleDTO;
import org.springframework.stereotype.Component;

@Component
public class CustomRoleMapper {

    public CustomRoleDTO toDto(CustomRole role) {
        return new CustomRoleDTO(
                role.getId(),
                role.getTenantId(),
                role.getName(),
                role.getDescription(),
                role.getPermissions(),
                role.isEnabled(),
                role.getCreatedAt()
        );
    }
}
