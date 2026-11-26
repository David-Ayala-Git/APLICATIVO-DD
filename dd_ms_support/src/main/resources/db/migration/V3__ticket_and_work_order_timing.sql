-- Registro de inicio y terminacion para tickets y ordenes de trabajo (calculo de duracion del proceso).
ALTER TABLE support.ticket
    ADD COLUMN IF NOT EXISTS started_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS finished_at TIMESTAMPTZ;

ALTER TABLE support.work_order
    ADD COLUMN IF NOT EXISTS started_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS finished_at TIMESTAMPTZ;
