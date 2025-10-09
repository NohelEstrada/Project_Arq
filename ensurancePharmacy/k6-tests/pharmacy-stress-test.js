import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// Métricas personalizadas
const errorRate = new Rate('errors');
const requestDuration = new Trend('request_duration');
const requestCount = new Counter('request_count');

// Configuración del test
export const options = {
  // Escenarios de carga
  stages: [
    { duration: '30s', target: 10 },  // Ramp-up a 10 usuarios en 30s
    { duration: '1m', target: 50 },   // Aumentar a 50 usuarios en 1 minuto
    { duration: '2m', target: 50 },   // Mantener 50 usuarios por 2 minutos
    { duration: '30s', target: 0 },   // Ramp-down a 0 en 30s
  ],
  
  // Thresholds - Criterios de éxito
  thresholds: {
    'http_req_duration': ['p(95)<1000', 'p(99)<2000'], // 95% < 1s, 99% < 2s
    'http_req_failed': ['rate<0.01'],                  // Error rate < 1%
    'errors': ['rate<0.01'],                           // Custom error rate < 1%
  },
  
  // Opciones adicionales
  noConnectionReuse: false,
  userAgent: 'k6-stress-test/1.0',
};

// Variables de entorno (pueden sobrescribirse desde CLI)
const BASE_URL = __ENV.BASE_URL || 'http://localhost';
const PORT = __ENV.PORT || '8101';
const API_PREFIX = __ENV.API_PREFIX || 'api2';

// Construcción de URLs
const API_BASE = `${BASE_URL}:${PORT}/${API_PREFIX}`;

export default function () {
  // Grupo de tests para mejor organización en los reportes
  const responses = {};
  
  // 1. GET Medicines
  let medicinesRes = http.get(`${API_BASE}/medicines`, {
    tags: { name: 'ListMedicines' },
  });
  
  check(medicinesRes, {
    'GET Medicines - Status 200': (r) => r.status === 200,
    'GET Medicines - Response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);
  
  requestDuration.add(medicinesRes.timings.duration);
  requestCount.add(1);
  
  // Tiempo de espera entre requests (think time)
  sleep(Math.random() * 2 + 1); // 1-3 segundos aleatorio
  
  // 2. GET Categories
  let categoriesRes = http.get(`${API_BASE}/categories`, {
    tags: { name: 'ListCategories' },
  });
  
  check(categoriesRes, {
    'GET Categories - Status 200': (r) => r.status === 200,
    'GET Categories - Response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);
  
  requestDuration.add(categoriesRes.timings.duration);
  requestCount.add(1);
  
  sleep(Math.random() * 2 + 1);
  
  // 3. GET Prescriptions
  let prescriptionsRes = http.get(`${API_BASE}/prescriptions`, {
    tags: { name: 'ListPrescriptions' },
  });
  
  check(prescriptionsRes, {
    'GET Prescriptions - Status 200': (r) => r.status === 200,
    'GET Prescriptions - Response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);
  
  requestDuration.add(prescriptionsRes.timings.duration);
  requestCount.add(1);
  
  sleep(Math.random() * 2 + 1);
  
  // 4. GET Medicine by ID (ID aleatorio entre 1 y 10)
  const medicineId = Math.floor(Math.random() * 10) + 1;
  let medicineDetailRes = http.get(`${API_BASE}/medicines/${medicineId}`, {
    tags: { name: 'GetMedicineDetail' },
  });
  
  check(medicineDetailRes, {
    'GET Medicine Detail - Status 200 or 404': (r) => r.status === 200 || r.status === 404,
    'GET Medicine Detail - Response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);
  
  requestDuration.add(medicineDetailRes.timings.duration);
  requestCount.add(1);
  
  sleep(Math.random() * 2 + 1);
}

// Función de setup (se ejecuta una vez antes de todos los VUs)
export function setup() {
  console.log('=================================');
  console.log('  K6 Stress Test - Pharmacy API');
  console.log('=================================');
  console.log(`Target: ${API_BASE}`);
  console.log(`Scenarios: Ramp-up to 50 users`);
  console.log('=================================\n');
  
  // Verificar que el backend esté disponible
  const healthCheck = http.get(`${API_BASE}/medicines`);
  if (healthCheck.status !== 200) {
    console.error(`ERROR: Backend not responding (status: ${healthCheck.status})`);
    console.error('Make sure the backend is running before starting the test');
  }
  
  return { startTime: new Date() };
}

// Función de teardown (se ejecuta una vez al final)
export function teardown(data) {
  const endTime = new Date();
  const duration = (endTime - data.startTime) / 1000;
  console.log('\n=================================');
  console.log('  Test Completed');
  console.log('=================================');
  console.log(`Total duration: ${duration.toFixed(2)} seconds`);
  console.log('Check the summary above for detailed metrics');
  console.log('=================================\n');
}

// Función para generar resumen personalizado
export function handleSummary(data) {
  return {
    'results/summary.json': JSON.stringify(data, null, 2),
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
  };
}

function textSummary(data, options) {
  const indent = options.indent || '';
  
  let summary = '\n';
  summary += indent + '=================================\n';
  summary += indent + '  K6 Test Summary\n';
  summary += indent + '=================================\n\n';
  
  // Métricas principales
  const metrics = data.metrics || {};
  
  if (metrics.http_reqs && metrics.http_reqs.values) {
    summary += indent + `Total Requests: ${metrics.http_reqs.values.count || 0}\n`;
  }
  
  if (metrics.http_req_duration && metrics.http_req_duration.values) {
    const avg = metrics.http_req_duration.values.avg || 0;
    const p95 = metrics.http_req_duration.values['p(95)'] || 0;
    const p99 = metrics.http_req_duration.values['p(99)'] || 0;
    summary += indent + `Avg Response Time: ${avg.toFixed(2)}ms\n`;
    summary += indent + `95th Percentile: ${p95.toFixed(2)}ms\n`;
    summary += indent + `99th Percentile: ${p99.toFixed(2)}ms\n`;
  }
  
  if (metrics.http_req_failed && metrics.http_req_failed.values) {
    const failRate = (metrics.http_req_failed.values.rate || 0) * 100;
    summary += indent + `Error Rate: ${failRate.toFixed(2)}%\n`;
  }
  
  summary += indent + '\n=================================\n\n';
  
  return summary;
}

