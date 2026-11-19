package dd.conexiones.dd_ms_network.infrastructure.client;

import dd.conexiones.dd_ms_network.domain.exception.InvalidRouterOsIdException;
import dd.conexiones.dd_ms_network.domain.model.RouterOsIds;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RouterOsIdsTest {

    @ParameterizedTest
    @ValueSource(strings = {"*1A", "*0", "cliente.123", "1098765432", "juan_perez@fibra", "plan-50M"})
    void aceptaIdsYNombresDeRouterOs(String value) {
        assertThat(RouterOsIds.require(value)).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "cliente 1", "x password=1234", "x=y", "\"x\"", "*1 disabled=no",
            "-x", "x\ny", "*XYZ", "?name=admin"})
    void rechazaValoresQueAgregarianParametros(String value) {
        assertThatThrownBy(() -> RouterOsIds.require(value)).isInstanceOf(InvalidRouterOsIdException.class);
    }

    @org.junit.jupiter.api.Test
    void rechazaNulo() {
        assertThatThrownBy(() -> RouterOsIds.require(null)).isInstanceOf(InvalidRouterOsIdException.class);
    }
}
