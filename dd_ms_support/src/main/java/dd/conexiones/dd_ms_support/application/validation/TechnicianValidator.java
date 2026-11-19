package dd.conexiones.dd_ms_support.application.validation;

import dd.conexiones.dd_ms_support.domain.exception.TechnicianNotFoundException;
import dd.conexiones.dd_ms_support.domain.repository.TechnicianRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TechnicianValidator {

    private final TechnicianRepository technicianRepository;

    public TechnicianValidator(TechnicianRepository technicianRepository) {
        this.technicianRepository = technicianRepository;
    }

    public void validateExists(UUID technicianId) {
        if (!technicianRepository.existsById(technicianId)) {
            throw new TechnicianNotFoundException(technicianId.toString());
        }
    }
}
