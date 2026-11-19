package dd.conexiones.dd_ms_reports.domain.exception;

public class SnapshotNotFoundException extends RuntimeException {
    public SnapshotNotFoundException(String period) {
        super("No hay reporte ejecutivo generado para el periodo: " + period);
    }
}
