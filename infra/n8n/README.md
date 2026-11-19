# Automatizaciones con n8n

Tres flujos de la guía (sección 10.5), versionados en `workflows/`:

| Flujo | Disparador | Qué hace |
|---|---|---|
| `corte-por-mora.json` | Todos los días, 6:00 (Bogotá) | `POST /api/billing/overdue` marca y trae las facturas vencidas → para cada cliente **ACTIVO** con router y usuario PPPoE: corta en MikroTik, lo marca `SUSPENDED` y avisa por WhatsApp (`aviso_corte`). |
| `reactivacion-por-pago.json` | Webhook `payment-confirmed` (lo llama `dd_ms_billing`) | Si el cliente estaba **SUSPENDED**: reactiva en MikroTik, lo marca `ACTIVE` y confirma por WhatsApp (`confirmacion_pago`). |
| `onboarding-cliente.json` | Webhook `client-created` (lo llama `dd_ms_crm`) | Crea la orden de instalación y envía la bienvenida (`bienvenida`). La autorización de la ONU queda fuera hasta integrar GPON real. |

No edites el JSON a mano: cambia `generate-workflows.js` y ejecuta `node infra/n8n/generate-workflows.js`.

## Puesta en marcha

1. **Crea un usuario de automatización** por ISP, con rol **Admin del ISP** (Configuración → Usuarios). Usa un correo propio, como `automatizacion@<isp>.co`, para que la auditoría distinga lo que hizo n8n.
2. **Agrega al `.env`** de la raíz:

   ```env
   AUTOMATION_SECRET=<cadena larga aleatoria>   # lo envían billing/crm y lo valida n8n
   N8N_TENANT_SLUG=dd-conexiones                # el ISP que atiende este n8n
   N8N_API_EMAIL=automatizacion@ddconexiones.co
   N8N_API_PASSWORD=<su contraseña>
   ```

   Sin `AUTOMATION_SECRET`, los webhooks no ejecutan nada.
3. **Levanta e importa:**

   ```sh
   docker compose up -d n8n
   docker compose exec n8n n8n import:workflow --separate --input=/workflows
   ```

4. **Activa** los tres flujos en http://localhost:5678. Se importan desactivados.
5. **Plantillas de WhatsApp:** `aviso_corte`, `confirmacion_pago` y `bienvenida` deben estar aprobadas en Meta, con el nombre del cliente como único parámetro. Si un envío falla, el flujo sigue: el corte o la reactivación no se deshacen.

## Requisitos de los datos

- El cliente necesita **Router MikroTik** y **Usuario PPPoE** en su ficha del CRM. Sin ellos no se corta ni se reactiva automáticamente; lo hace el equipo a mano desde Activaciones.
- Los módulos usados deben estar activos en el ISP: facturación, CRM, red y comunicaciones (más soporte, para el onboarding).

## Límites conocidos

- **Un n8n atiende a un ISP.** Los webhooks descartan eventos de otro tenant. Para varios ISP, levanta una instancia de n8n por ISP, o duplica los flujos con su propio usuario y slug.
- **Los eventos son "dispara y olvida".** Si n8n está caído cuando entra un pago, la reactivación no ocurre sola; hay que hacerla desde la consola.
- **Los flujos no se han probado contra un n8n real.** Se generan y validan con `generate-workflows.js`; al importarlos por primera vez, revisa una ejecución de cada uno.
