package dd.conexiones.dd_ms_network.infrastructure.client;

import dd.conexiones.dd_ms_network.domain.exception.RouterConnectionException;
import dd.conexiones.dd_ms_network.domain.model.Router;
import dd.conexiones.dd_ms_network.domain.model.RouterOsIds;
import dd.conexiones.dd_ms_network.infrastructure.security.CryptoService;
import me.legrange.mikrotik.ApiConnection;

import javax.net.SocketFactory;
import org.springframework.stereotype.Component;

/**
 * Envoltorio sobre la API de MikroTik RouterOS (me.legrange:mikrotik).
 * Cada llamada abre una conexion nueva, autentica y la cierra: simple y suficiente
 * para el volumen de operaciones (cortes/reactivaciones/colas) de un ISP mediano.
 *
 * Nota: requiere conectividad real a un router MikroTik en apiPort (8728/8729).
 * No hay hardware disponible en este entorno de desarrollo para probar contra un
 * equipo real; la implementacion sigue el patron oficial de la libreria.
 */
@Component
public class MikroTikClient {

    private final CryptoService cryptoService;

    public MikroTikClient(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public void disableSecret(Router router, String secretId) {
        execute(router, "/ppp/secret/set numbers=" + RouterOsIds.require(secretId) + " disabled=yes");
    }

    public void enableSecret(Router router, String secretId) {
        execute(router, "/ppp/secret/set numbers=" + RouterOsIds.require(secretId) + " disabled=no");
    }

    public void setQueueLimit(Router router, String queueId, int uploadKbps, int downloadKbps) {
        execute(router, "/queue/simple/set numbers=" + RouterOsIds.require(queueId)
                + " max-limit=" + uploadKbps + "k/" + downloadKbps + "k");
    }

    // RouterOsIds.require se evalua antes de entrar aqui, asi que un id invalido
    // responde 400 y nunca abre conexion con el router.
    private void execute(Router router, String command) {
        try {
            // connect(host) siempre usa 8728: respeta el puerto configurado del router
            int port = router.getApiPort() == null ? ApiConnection.DEFAULT_PORT : router.getApiPort();
            ApiConnection connection = ApiConnection.connect(SocketFactory.getDefault(), router.getHost(),
                    port, ApiConnection.DEFAULT_CONNECTION_TIMEOUT);
            try {
                connection.login(router.getApiUser(), cryptoService.decrypt(router.getApiPasswordEnc()));
                connection.execute(command);
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            throw new RouterConnectionException(router.getName(), e);
        }
    }
}
