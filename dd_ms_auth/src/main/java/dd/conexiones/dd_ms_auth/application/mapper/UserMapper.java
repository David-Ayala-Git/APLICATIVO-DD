package dd.conexiones.dd_ms_auth.application.mapper;

import dd.conexiones.dd_ms_auth.domain.model.AppUser;
import dd.conexiones.dd_ms_auth.web.dto.UserSummaryDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserSummaryDTO toDto(AppUser user) {
        return new UserSummaryDTO(user.getId(), user.getEmail(), user.getFullName(), user.getRole(),
                user.getTenantId(), user.getCustomRoleId());
    }
}
