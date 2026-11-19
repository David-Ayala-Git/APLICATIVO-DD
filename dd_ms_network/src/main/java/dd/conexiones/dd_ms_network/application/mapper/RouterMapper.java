package dd.conexiones.dd_ms_network.application.mapper;

import dd.conexiones.dd_ms_network.domain.model.Router;
import dd.conexiones.dd_ms_network.web.dto.RouterDTO;
import org.springframework.stereotype.Component;

@Component
public class RouterMapper {

    public RouterDTO toDto(Router router) {
        return new RouterDTO(router.getId(), router.getName(), router.getHost(), router.getApiPort(),
                router.getApiUser(), router.getModel(), router.getStatus(), router.getCreatedAt());
    }
}
