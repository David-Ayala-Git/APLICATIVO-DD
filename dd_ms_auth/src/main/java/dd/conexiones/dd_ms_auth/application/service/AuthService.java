package dd.conexiones.dd_ms_auth.application.service;

import dd.conexiones.dd_ms_auth.web.dto.LoginRequest;
import dd.conexiones.dd_ms_auth.web.dto.LoginResponse;

public interface AuthService {
    /** @param clientIp IP de origen, para el limite de intentos fallidos */
    LoginResponse login(LoginRequest request, String clientIp);
}
