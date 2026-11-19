package dd.conexiones.dd_ms_billing.web.dto;

public record BillingRunResult(
        String period,
        int clientsEvaluated,
        int invoicesCreated,
        int invoicesSkipped
) {
}
