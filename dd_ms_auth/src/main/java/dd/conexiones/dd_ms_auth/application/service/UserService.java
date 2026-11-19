package dd.conexiones.dd_ms_auth.application.service;

import dd.conexiones.dd_ms_auth.web.dto.CreateUserRequest;
import dd.conexiones.dd_ms_auth.web.dto.UserSummaryDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserSummaryDTO> findAll();
    UserSummaryDTO create(CreateUserRequest request);
    UserSummaryDTO findById(UUID id);
}
