package dd.conexiones.mscommon.role;

public class RoleNotAllowedException extends RuntimeException {
    public RoleNotAllowedException(String role) {
        super(role == null
                ? "La peticion no trae un rol de usuario (X-User-Role)"
                : "El rol '" + role + "' no tiene permiso para esta accion");
    }
}
