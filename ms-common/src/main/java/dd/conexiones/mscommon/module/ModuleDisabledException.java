package dd.conexiones.mscommon.module;

public class ModuleDisabledException extends RuntimeException {
    public ModuleDisabledException(String moduleKey) {
        super("El modulo '" + moduleKey + "' no esta habilitado para este tenant");
    }
}
