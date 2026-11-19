package dd.conexiones.mscommon.tenant;

/**
 * Se cargo una entidad de otro tenant. Cada servicio la responde como 404 (no
 * 403): confirmar que el registro existe ya filtraria informacion de otro ISP.
 */
public class CrossTenantAccessException extends RuntimeException {
    public CrossTenantAccessException(String entity) {
        super("Registro no encontrado: " + entity);
    }
}
