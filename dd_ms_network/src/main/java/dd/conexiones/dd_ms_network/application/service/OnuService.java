package dd.conexiones.dd_ms_network.application.service;

import dd.conexiones.dd_ms_network.web.dto.AuthorizeOnuRequest;
import dd.conexiones.dd_ms_network.web.dto.OnuDTO;
import dd.conexiones.dd_ms_network.web.dto.RegisterOnuRequest;
import dd.conexiones.dd_ms_network.web.dto.SignalUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface OnuService {
    List<OnuDTO> findAll();
    OnuDTO findById(UUID id);
    OnuDTO register(RegisterOnuRequest request);
    OnuDTO authorize(UUID id, AuthorizeOnuRequest request);
    OnuDTO updateSignal(UUID id, SignalUpdateRequest request);
}
