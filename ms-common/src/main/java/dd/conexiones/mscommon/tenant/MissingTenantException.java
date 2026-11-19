package dd.conexiones.mscommon.tenant;

public class MissingTenantException extends RuntimeException {
    public MissingTenantException() {
        super("El header X-Tenant-Id es obligatorio");
    }
}
