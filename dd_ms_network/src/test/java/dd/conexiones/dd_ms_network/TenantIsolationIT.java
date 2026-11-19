package dd.conexiones.dd_ms_network;

import dd.conexiones.dd_ms_network.domain.model.Onu;
import dd.conexiones.dd_ms_network.domain.model.enums.OnuVendor;
import dd.conexiones.dd_ms_network.domain.repository.OnuRepository;
import dd.conexiones.mscommon.tenant.CrossTenantAccessException;
import dd.conexiones.mscommon.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Guia, seccion 12: "pruebalo con tests que intenten leer datos de otro tenant y
 * deban fallar". Corre contra la base del perfil dev (Hibernate y Postgres reales,
 * incluido el listener de ms-common) dentro de una transaccion que se revierte:
 * no deja datos.
 */
@SpringBootTest(properties = "spring.cloud.consul.discovery.register=false")
@Transactional
class TenantIsolationIT {

    private static final String ISP_A = "prueba-aislamiento-a";
    private static final String ISP_B = "prueba-aislamiento-b";

    @Autowired
    private OnuRepository onuRepository;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    /** Guarda una ONU del ISP A y vacia el contexto de persistencia, como una peticion nueva. */
    private UUID onuOfIspA() {
        Onu onu = new Onu();
        onu.setTenantId(ISP_A);
        onu.setSerial("TEST-" + UUID.randomUUID().toString().substring(0, 8));
        onu.setVendor(OnuVendor.values()[0]);
        onu.setOltName("OLT-PRUEBA");
        onu.setPonPort("0/1/1");
        UUID id = onuRepository.saveAndFlush(onu).getId();
        entityManager.clear();
        return id;
    }

    @Test
    void otroIspNoPuedeCargarPorId() {
        UUID id = onuOfIspA();
        TenantContext.set(ISP_B);
        assertThatThrownBy(() -> onuRepository.findById(id))
                .isInstanceOf(CrossTenantAccessException.class);
    }

    @Test
    void elDuenoSiPuedeCargarPorId() {
        UUID id = onuOfIspA();
        TenantContext.set(ISP_A);
        assertThat(onuRepository.findById(id)).isPresent();
    }

    @Test
    void sinTenantEnContextoNoSeRevisa() {
        // webhooks de terceros resuelven el dueno desde el propio registro
        UUID id = onuOfIspA();
        assertThat(onuRepository.findById(id)).isPresent();
    }
}
