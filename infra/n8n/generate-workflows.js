// Genera los flujos de n8n de ./workflows a partir de esta descripcion compacta
// y valida que cada conexion apunte a un nodo existente.
//   node infra/n8n/generate-workflows.js
// Editar aqui y regenerar es mas seguro que tocar a mano el JSON exportado.
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const OUT = path.join(__dirname, 'workflows');
const API = '{{ $env.DD_API_URL }}';

// IDs estables entre regeneraciones: derivados del nombre, no aleatorios, para
// que el diff de git muestre solo cambios reales.
const stableId = (...parts) => {
  const h = crypto.createHash('sha1').update(parts.join('|')).digest('hex');
  return `${h.slice(0, 8)}-${h.slice(8, 12)}-${h.slice(12, 16)}-${h.slice(16, 20)}-${h.slice(20, 32)}`;
};

const auth = {
  sendHeaders: true,
  headerParameters: {
    parameters: [{ name: 'Authorization', value: "=Bearer {{ $('Login').first().json.token }}" }],
  },
};

const http = (name, method, url, body, extra = {}) => ({
  name,
  type: 'n8n-nodes-base.httpRequest',
  typeVersion: 4.2,
  parameters: {
    method,
    url: '=' + API + url,
    ...auth,
    ...(body ? { sendBody: true, specifyBody: 'json', jsonBody: '={{ JSON.stringify(' + body + ') }}' } : {}),
    options: {},
  },
  ...extra,
});

// Usuario ADMIN_ISP dedicado a la automatizacion; sus credenciales vienen del
// entorno de n8n, nunca de este JSON.
const login = () => ({
  name: 'Login',
  type: 'n8n-nodes-base.httpRequest',
  typeVersion: 4.2,
  parameters: {
    method: 'POST',
    url: '=' + API + '/api/auth/login',
    sendBody: true,
    specifyBody: 'json',
    jsonBody: '={{ JSON.stringify({ email: $env.DD_API_EMAIL, password: $env.DD_API_PASSWORD }) }}',
    options: {},
  },
  // un solo login por ejecucion aunque lleguen varios items
  executeOnce: true,
});

const cond = (flow, left, op, right = '') => ({
  id: stableId(flow, left, op, right),
  leftValue: '=' + left,
  rightValue: right,
  operator:
    op === 'notEmpty'
      ? { type: 'string', operation: 'notEmpty', singleValue: true }
      : { type: 'string', operation: 'equals' },
});

const ifNode = (name, conditions) => ({
  name,
  type: 'n8n-nodes-base.if',
  typeVersion: 2,
  parameters: {
    conditions: {
      options: { caseSensitive: true, leftValue: '', typeValidation: 'loose' },
      conditions,
      combinator: 'and',
    },
    options: {},
  },
});

const webhook = (name, webhookPath) => ({
  name,
  type: 'n8n-nodes-base.webhook',
  typeVersion: 2,
  parameters: { httpMethod: 'POST', path: webhookPath, responseMode: 'onReceived', options: {} },
  webhookId: stableId('webhook', webhookPath),
});

// Sin secreto configurado no se acepta nada (vacio == vacio no debe pasar), y
// cada n8n atiende a un solo ISP: el evento debe ser de DD_TENANT_SLUG.
const verified = (flow) =>
  ifNode('Evento autentico', [
    cond(flow, '{{ $env.DD_AUTOMATION_SECRET }}', 'notEmpty'),
    cond(flow, "{{ $json.headers['x-dd-automation-secret'] }}", 'equals', '={{ $env.DD_AUTOMATION_SECRET }}'),
    cond(flow, '{{ $json.body.tenantId }}', 'equals', '={{ $env.DD_TENANT_SLUG }}'),
  ]);

// Un WhatsApp fallido (cliente sin telefono, plantilla no aprobada) no debe
// deshacer ni frenar lo que ya se hizo en la red.
const softFail = { onError: 'continueRegularOutput' };

const seq = (...names) => names.slice(1).map((to, i) => [names[i], to]);

function workflow(name, nodes, chain, notes) {
  nodes.forEach((node, i) => {
    node.id = stableId(name, node.name);
    node.position = [i * 240, 300];
  });
  const names = new Set(nodes.map((n) => n.name));
  if (names.size !== nodes.length) throw new Error(`${name}: nombres de nodo repetidos`);
  const connections = {};
  for (const [from, to, branch = 0] of chain) {
    if (!names.has(from) || !names.has(to)) throw new Error(`${name}: conexion invalida ${from} -> ${to}`);
    const outs = (connections[from] ??= { main: [] }).main;
    while (outs.length <= branch) outs.push([]);
    outs[branch].push({ node: to, type: 'main', index: 0 });
  }
  return {
    name,
    nodes,
    connections,
    active: false,
    settings: { executionOrder: 'v1', timezone: 'America/Bogota' },
    meta: { notes },
  };
}

const CORTE = 'D&D · Corte por mora';
const REACTIVACION = 'D&D · Reactivacion por pago';
const ONBOARDING = 'D&D · Onboarding de cliente';

const flows = {
  'corte-por-mora.json': workflow(
    CORTE,
    [
      {
        name: 'Cada dia 6am',
        type: 'n8n-nodes-base.scheduleTrigger',
        typeVersion: 1.2,
        parameters: { rule: { interval: [{ field: 'cronExpression', expression: '0 6 * * *' }] } },
      },
      login(),
      // marca vencidas y devuelve TODAS las vencidas: un corte fallido ayer se reintenta hoy
      http('Facturas vencidas', 'POST', '/api/billing/overdue'),
      http('Obtener cliente', 'GET', '/api/crm/clients/{{ $json.clientId }}'),
      ifNode('Activo y con PPPoE', [
        cond(CORTE, '{{ $json.status }}', 'equals', 'ACTIVE'),
        cond(CORTE, '{{ $json.routerId }}', 'notEmpty'),
        cond(CORTE, '{{ $json.pppoeUser }}', 'notEmpty'),
      ]),
      http(
        'Cortar en MikroTik',
        'POST',
        "/api/network/routers/{{ $('Obtener cliente').item.json.routerId }}/cut",
        "{ secretId: $('Obtener cliente').item.json.pppoeUser }",
      ),
      http(
        'Marcar suspendido',
        'POST',
        "/api/crm/clients/{{ $('Obtener cliente').item.json.id }}/status",
        "{ status: 'SUSPENDED' }",
      ),
      http(
        'Avisar por WhatsApp',
        'POST',
        '/api/whatsapp/send',
        "{ phone: $json.phone, templateName: 'aviso_corte', bodyParams: [$json.fullName] }",
        softFail,
      ),
    ],
    [
      ...seq('Cada dia 6am', 'Login', 'Facturas vencidas', 'Obtener cliente', 'Activo y con PPPoE'),
      ['Activo y con PPPoE', 'Cortar en MikroTik', 0],
      ...seq('Cortar en MikroTik', 'Marcar suspendido', 'Avisar por WhatsApp'),
    ],
    'Corta en MikroTik a los clientes ACTIVOS con factura vencida que tengan router y usuario PPPoE en su ficha. Los SUSPENDED no se vuelven a cortar.',
  ),

  'reactivacion-por-pago.json': workflow(
    REACTIVACION,
    [
      webhook('Pago confirmado', 'payment-confirmed'),
      verified(REACTIVACION),
      login(),
      http('Obtener cliente', 'GET', "/api/crm/clients/{{ $('Pago confirmado').item.json.body.clientId }}"),
      ifNode('Suspendido y con PPPoE', [
        cond(REACTIVACION, '{{ $json.status }}', 'equals', 'SUSPENDED'),
        cond(REACTIVACION, '{{ $json.routerId }}', 'notEmpty'),
        cond(REACTIVACION, '{{ $json.pppoeUser }}', 'notEmpty'),
      ]),
      http(
        'Reactivar en MikroTik',
        'POST',
        "/api/network/routers/{{ $('Obtener cliente').item.json.routerId }}/enable",
        "{ secretId: $('Obtener cliente').item.json.pppoeUser }",
      ),
      http(
        'Marcar activo',
        'POST',
        "/api/crm/clients/{{ $('Obtener cliente').item.json.id }}/status",
        "{ status: 'ACTIVE' }",
      ),
      http(
        'Confirmar por WhatsApp',
        'POST',
        '/api/whatsapp/send',
        "{ phone: $json.phone, templateName: 'confirmacion_pago', bodyParams: [$json.fullName] }",
        softFail,
      ),
    ],
    [
      ...seq('Pago confirmado', 'Evento autentico'),
      ['Evento autentico', 'Login', 0],
      ...seq('Login', 'Obtener cliente', 'Suspendido y con PPPoE'),
      ['Suspendido y con PPPoE', 'Reactivar en MikroTik', 0],
      ...seq('Reactivar en MikroTik', 'Marcar activo', 'Confirmar por WhatsApp'),
    ],
    'dd_ms_billing llama este webhook al confirmar un pago (AUTOMATION_WEBHOOK_BASE_URL). Solo reactiva a quien estaba SUSPENDED.',
  ),

  'onboarding-cliente.json': workflow(
    ONBOARDING,
    [
      webhook('Cliente creado', 'client-created'),
      verified(ONBOARDING),
      login(),
      http(
        'Orden de instalacion',
        'POST',
        '/api/technicians/orders',
        "{ clientId: $('Cliente creado').item.json.body.clientId, type: 'INSTALLATION', address: $('Cliente creado').item.json.body.address || null }",
        softFail,
      ),
      http(
        'Bienvenida por WhatsApp',
        'POST',
        '/api/whatsapp/send',
        "{ phone: $('Cliente creado').item.json.body.phone, templateName: 'bienvenida', bodyParams: [$('Cliente creado').item.json.body.fullName] }",
        softFail,
      ),
    ],
    [
      ...seq('Cliente creado', 'Evento autentico'),
      ['Evento autentico', 'Login', 0],
      ...seq('Login', 'Orden de instalacion', 'Bienvenida por WhatsApp'),
    ],
    'dd_ms_crm llama este webhook al crear un cliente. La autorizacion de la ONU queda fuera hasta integrar GPON real.',
  ),
};

fs.mkdirSync(OUT, { recursive: true });
for (const [file, wf] of Object.entries(flows)) {
  fs.writeFileSync(path.join(OUT, file), JSON.stringify(wf, null, 2) + '\n');
  const edges = Object.values(wf.connections).flatMap((c) => c.main.flat()).length;
  console.log(`ok ${file}: ${wf.nodes.length} nodos, ${edges} conexiones`);
}
