package dd.conexiones.dd_ms_network.domain.repository;

import dd.conexiones.dd_ms_network.domain.model.Node;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NodeRepository extends JpaRepository<Node, UUID> {
}
