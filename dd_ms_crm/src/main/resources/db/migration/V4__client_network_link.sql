-- Con que router MikroTik y usuario PPPoE navega el cliente: el flujo de n8n de
-- corte por mora / reactivacion los necesita para deshabilitar o habilitar su
-- secret. router_id apunta a network.router, pero sin FK: cada servicio es dueno
-- de su schema (guia, seccion 1.2).
ALTER TABLE crm.client
  ADD COLUMN router_id   UUID,
  ADD COLUMN pppoe_user  VARCHAR(64);
