package dd.conexiones.dd_ms_crm.application.mapper;

import dd.conexiones.dd_ms_crm.domain.model.Contract;
import dd.conexiones.dd_ms_crm.web.dto.ContractDTO;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {

    public ContractDTO toDto(Contract contract) {
        return new ContractDTO(contract.getId(), contract.getClientId(), contract.getPlanId(),
                contract.getStatus(), contract.getSignedAt(), contract.getDocumentUrl(), contract.getCreatedAt());
    }
}
