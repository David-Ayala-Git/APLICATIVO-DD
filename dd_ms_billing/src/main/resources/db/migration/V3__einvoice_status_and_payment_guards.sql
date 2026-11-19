-- Facturacion electronica DIAN: todavia sin proveedor tecnologico (ver
-- ElectronicInvoiceProvider). NOT_ISSUED = la factura existe en el sistema pero
-- aun no se emitio ante la DIAN; nunca se inventa un CUFE.
ALTER TABLE billing.invoice
  ADD COLUMN einvoice_status VARCHAR(20) NOT NULL DEFAULT 'NOT_ISSUED',
  ADD COLUMN einvoice_error  VARCHAR(500);

UPDATE billing.invoice SET einvoice_status = 'ISSUED' WHERE cufe IS NOT NULL;

-- Una misma transaccion (referencia de la pasarela o comprobante manual) no puede
-- registrarse dos veces: las pasarelas reintentan sus webhooks.
CREATE UNIQUE INDEX uq_payment_external_reference
  ON billing.payment (tenant_id, method, external_reference)
  WHERE external_reference IS NOT NULL;
