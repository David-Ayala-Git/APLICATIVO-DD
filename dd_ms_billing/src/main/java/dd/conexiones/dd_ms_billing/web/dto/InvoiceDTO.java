package dd.conexiones.dd_ms_billing.web.dto;

import dd.conexiones.dd_ms_billing.domain.model.enums.ElectronicInvoiceStatus;
import dd.conexiones.dd_ms_billing.domain.model.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InvoiceDTO(
        UUID id,
        UUID clientId,
        UUID planId,
        String period,
        BigDecimal amount,
        InvoiceStatus status,
        LocalDate dueDate,
        OffsetDateTime issuedAt,
        String cufe,
        String pdfUrl,
        String xmlUrl,
        ElectronicInvoiceStatus einvoiceStatus,
        String einvoiceError
) {
}
