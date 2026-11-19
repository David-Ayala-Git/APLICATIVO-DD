package dd.conexiones.dd_ms_comms.domain.repository;

import dd.conexiones.dd_ms_comms.domain.model.WhatsappMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WhatsappMessageRepository extends JpaRepository<WhatsappMessage, UUID> {

    /**
     * Sin filtro de tenant a proposito: un webhook entrante de Meta no trae
     * X-Tenant-Id, asi que el tenant se resuelve buscando en toda la tabla la
     * conversacion mas reciente con ese numero (ver {@code receiveIncoming}).
     */
    Optional<WhatsappMessage> findTopByPhoneOrderByCreatedAtDesc(String phone);
}
