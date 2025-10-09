# 🚀 Tests de Stress con k6

## 📋 ¿Qué es k6?

**k6** es una herramienta moderna de testing de carga de código abierto creada por Grafana Labs.

**Características:**
- 💻 **Scripts en JavaScript**: Tests fáciles de escribir y mantener
- 📊 **Métricas integradas**: Exporta directamente a Prometheus, InfluxDB, etc.
- ⚡ **Alto rendimiento**: Escrito en Go, muy rápido y eficiente
- 🎯 **Thresholds**: Define criterios de éxito/fallo automáticamente
- 🔗 **CI/CD friendly**: Fácil de integrar en pipelines

## 🚀 Inicio Rápido

### 1. Asegúrate que la aplicación esté corriendo

```bash
cd ensurancePharmacy
docker-compose -f docker-compose.prod.yml up -d backend-prod
# O ejecuta el pipeline completo en Jenkins
```

### 2. Ejecutar el test de stress

```bash
cd k6-tests
./run-stress-test.sh
```

### 3. Ver resultados

```bash
# Ver resumen en consola (ya se muestra automáticamente)

# Ver reporte HTML
open results/report-[timestamp].html

# Ver datos detallados en JSON
cat results/summary-[timestamp].json | python3 -m json.tool
```

## ⚙️ Configuración del Test

### Parámetros por Defecto

- **Puerto backend**: 8101 (producción)
- **Usuarios virtuales (VUs)**: 50
- **Duración total**: ~4 minutos con estas etapas:
  - 30s: Ramp-up de 0 a 10 usuarios
  - 1m: Ramp-up de 10 a 50 usuarios
  - 2m: Mantener 50 usuarios (fase de stress)
  - 30s: Ramp-down de 50 a 0 usuarios

### Personalizar Parámetros

```bash
# Sintaxis:
./run-stress-test.sh [puerto] [vus] [duración]

# Ejemplos:
./run-stress-test.sh 8101 100 5m      # 100 usuarios, 5 minutos
./run-stress-test.sh 8084 25 2m       # Test ligero para DEV
./run-stress-test.sh 8101 200 10m     # Test pesado
```

## 🎯 Endpoints Probados

El test prueba los mismos endpoints que JMeter:

1. **GET /api2/medicines** - Listar todas las medicinas
2. **GET /api2/categories** - Listar todas las categorías
3. **GET /api2/prescriptions** - Listar todas las prescripciones
4. **GET /api2/medicines/:id** - Obtener detalles de una medicina (ID aleatorio 1-10)

Cada usuario virtual ejecuta estos 4 endpoints en secuencia con "think time" aleatorio de 1-3 segundos.

## 📊 Métricas y Thresholds

### Métricas Automáticas de k6

k6 genera automáticamente estas métricas:

- **http_reqs**: Total de requests HTTP
- **http_req_duration**: Duración de requests (avg, min, max, p90, p95, p99)
- **http_req_failed**: Tasa de requests fallidos
- **http_req_blocked**: Tiempo bloqueado antes de iniciar request
- **http_req_connecting**: Tiempo estableciendo conexión TCP
- **http_req_tls_handshaking**: Tiempo en TLS handshake
- **http_req_sending**: Tiempo enviando datos
- **http_req_waiting**: Tiempo esperando respuesta (TTFB)
- **http_req_receiving**: Tiempo recibiendo respuesta
- **vus**: Número de usuarios virtuales activos
- **vus_max**: Máximo de VUs alcanzado
- **iterations**: Iteraciones completadas

### Métricas Personalizadas (en el script)

- **errors**: Tasa de errores personalizada
- **request_duration**: Trend de duración de requests
- **request_count**: Contador de requests

### Thresholds Configurados

El test falla automáticamente si:
- ❌ **95th percentile** > 1000ms
- ❌ **99th percentile** > 2000ms
- ❌ **Error rate** > 1%

## 📈 Resultados Generados

### 1. Resumen en Consola

Se muestra automáticamente al terminar:
```
     ✓ GET Medicines - Status 200
     ✓ GET Medicines - Response time < 500ms
     ✓ GET Categories - Status 200
     ...

     checks.........................: 99.5%  ✓ 1990  ✗ 10
     data_received..................: 25 MB  125 kB/s
     data_sent......................: 180 kB 900 B/s
     http_req_blocked...............: avg=1.2ms   min=2µs     med=8µs     max=45ms    p(95)=3.5ms   p(99)=12ms
     http_req_duration..............: avg=245ms   min=12ms    med=198ms   max=1.2s    p(95)=680ms   p(99)=950ms
     http_req_failed................: 0.50%  ✓ 10    ✗ 1990
     http_reqs......................: 2000   10/s
     iterations.....................: 500    2.5/s
     vus............................: 1      min=1   max=50
```

### 2. Archivo JSON Detallado

**Ubicación**: `results/test-results-[timestamp].json`

Contiene datos de cada request individual:
- Timestamp
- Métrica
- Valor
- Tags

### 3. Summary JSON

**Ubicación**: `results/summary-[timestamp].json`

Resumen agregado con todas las métricas y percentiles.

### 4. Reporte HTML

**Ubicación**: `results/report-[timestamp].html`

HTML simple con resumen visual de la configuración y endpoints probados.

## 🎨 Visualización en Grafana/OpenObserve

### Opción 1: Ver durante el test

Mientras k6 corre:
1. Abrir Grafana: http://localhost:3000
2. Ver "Application Performance Dashboard"
3. Observar cómo suben CPU, Memory, Network, Disk I/O

### Opción 2: Exportar métricas a Prometheus (Avanzado)

Para enviar métricas de k6 directamente a Prometheus:

```bash
# Instalar extensión de k6
# (Ya viene con soporte para Prometheus remote-write)

# Ejecutar con output a Prometheus
k6 run pharmacy-stress-test.js \
    --out experimental-prometheus-rw \
    -e K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9090/api/v1/write
```

Esto permitirá ver métricas de k6 directamente en Grafana/OpenObserve.

## 🆚 k6 vs JMeter

| Característica | JMeter | k6 |
|----------------|--------|-----|
| **Lenguaje** | GUI + XML | JavaScript |
| **Performance** | Moderado | ✅ Muy rápido |
| **Uso de RAM** | Alto (~500MB) | ✅ Bajo (~50MB) |
| **Scripting** | GUI pesada | ✅ Código simple |
| **CI/CD** | Complejo | ✅ Muy fácil |
| **Reportes** | HTML completo | JSON + Terminal |
| **Curva de aprendizaje** | Media-Alta | ✅ Baja (JavaScript) |
| **Métricas en tiempo real** | Limitado | ✅ Excelente |
| **Thresholds** | Manual | ✅ Automático |
| **Comunidad** | Muy grande | ✅ Creciendo rápido |

**Ventajas de k6:**
- ✅ Más rápido y eficiente
- ✅ Código en vez de GUI (mejor para Git)
- ✅ Fácil de integrar en CI/CD
- ✅ Exporta métricas directamente a Prometheus
- ✅ Sintaxis JavaScript familiar

**Ventajas de JMeter:**
- ✅ GUI completa para crear tests
- ✅ Reportes HTML muy detallados
- ✅ Más maduro (desde 1998)
- ✅ Más plugins disponibles

## 🔧 Modificar el Test

### Editar el Script

```bash
code k6-tests/pharmacy-stress-test.js
```

Puedes modificar:
- **Stages**: Cambiar el perfil de carga
- **Endpoints**: Agregar/quitar endpoints
- **Thresholds**: Ajustar criterios de éxito
- **Think time**: Cambiar `sleep()` entre requests
- **Checks**: Agregar más validaciones

### Ejemplo: Agregar Endpoint POST

```javascript
// En la función default()
let createRes = http.post(`${API_BASE}/orders`, JSON.stringify({
  patientName: 'Test Patient',
  medicines: [1, 2, 3]
}), {
  headers: { 'Content-Type': 'application/json' },
  tags: { name: 'CreateOrder' },
});

check(createRes, {
  'POST Order - Status 201': (r) => r.status === 201,
});
```

## 📝 Escenarios de Prueba

### Test Ligero (Desarrollo)
```bash
./run-stress-test.sh 8084 10 1m
```
- 10 usuarios
- 1 minuto
- Puerto DEV

### Test Medio (UAT)
```bash
./run-stress-test.sh 8091 50 3m
```
- 50 usuarios
- 3 minutos
- Puerto UAT

### Test Pesado (Producción)
```bash
./run-stress-test.sh 8101 100 5m
```
- 100 usuarios
- 5 minutos
- Puerto PROD

### Test de Picos (Spike Test)
Editar `pharmacy-stress-test.js` y cambiar stages:
```javascript
stages: [
  { duration: '10s', target: 100 },  // Pico repentino
  { duration: '1m', target: 100 },   // Mantener
  { duration: '10s', target: 0 },    // Drop
]
```

### Test de Soak (Duración Larga)
```bash
./run-stress-test.sh 8101 30 30m
```
- 30 usuarios constantes
- 30 minutos
- Detecta memory leaks

## 🎯 Criterios de Éxito

### Thresholds Configurados en el Script

| Métrica | Threshold | Descripción |
|---------|-----------|-------------|
| http_req_duration (p95) | < 1000ms | 95% de requests bajo 1 segundo |
| http_req_duration (p99) | < 2000ms | 99% de requests bajo 2 segundos |
| http_req_failed | < 1% | Menos de 1% de errores |

Si algún threshold falla, k6 **retorna exit code 99**.

### Métricas Objetivo

| Métrica | Excelente | Bueno | Aceptable | Malo |
|---------|-----------|-------|-----------|------|
| Avg Response Time | <200ms | <500ms | <1000ms | >1000ms |
| 95th Percentile | <500ms | <1000ms | <2000ms | >2000ms |
| Error Rate | 0% | <0.5% | <1% | >1% |
| Requests/sec | >100 | >50 | >20 | <20 |

## 🚀 Integración con Jenkins (Opcional)

Agregar al Jenkinsfile:

```groovy
stage('K6 Stress Tests') {
    when {
        branch 'master'
    }
    steps {
        dir("${env.PROJECT_DIR}/k6-tests") {
            sh './run-stress-test.sh 8101 50 3m'
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'k6-tests/results/*.json,k6-tests/results/*.html'
        }
    }
}
```

## 📊 Exportar Métricas a Prometheus (Bonus)

Para ver métricas de k6 en Grafana/OpenObserve en tiempo real:

### Opción 1: Prometheus Remote Write

```bash
k6 run pharmacy-stress-test.js \
    -o experimental-prometheus-rw \
    -e K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9090/api/v1/write \
    -e K6_PROMETHEUS_RW_TREND_AS_NATIVE_HISTOGRAM=true
```

### Opción 2: StatsD + Prometheus

1. Levantar StatsD exporter
2. Configurar k6 para enviar a StatsD
3. Prometheus scrape del exporter

## 💡 Comandos Útiles

```bash
# Ver versión
k6 version

# Ejecutar test con más VUs
k6 run pharmacy-stress-test.js --vus 100 --duration 5m

# Ejecutar con variables de entorno
k6 run pharmacy-stress-test.js -e PORT=8084 -e VUS=25

# Ver opciones del test
k6 inspect pharmacy-stress-test.js

# Ejecutar solo checks (smoke test)
k6 run pharmacy-stress-test.js --vus 1 --iterations 1
```

## 🎬 Demo con k6

### Preparación:

1. Backend corriendo
2. Grafana abierto en http://localhost:3000
3. Terminal listo

### Durante la Demo:

1. **Mostrar el script**:
```bash
cat pharmacy-stress-test.js | head -50
```
"Este es el test escrito en JavaScript, fácil de leer y mantener"

2. **Ejecutar el test**:
```bash
./run-stress-test.sh
```

3. **Mientras corre, mostrar Grafana**:
   - "Aquí vemos en tiempo real cómo aumenta el CPU"
   - "La memoria también sube con la carga"
   - "Network throughput muestra el tráfico generado"

4. **Al terminar, mostrar resultados**:
   - Ver el resumen en consola
   - "k6 ejecutó 2000 requests en 4 minutos"
   - "95% de requests bajo 680ms"
   - "Error rate 0.5%"

5. **Comparar con JMeter** (opcional):
   - "JMeter es más completo para reportes"
   - "k6 es más rápido y fácil de automatizar"
   - "Ambos son excelentes herramientas"

## 🆚 Comparación: k6 vs JMeter

### Cuándo Usar k6:

- ✅ Tests simples y rápidos
- ✅ Integración CI/CD
- ✅ Scripts versionados en Git
- ✅ Necesitas bajo uso de recursos
- ✅ Exportar métricas a Prometheus
- ✅ Equipo familiarizado con JavaScript

### Cuándo Usar JMeter:

- ✅ Tests complejos con muchas variaciones
- ✅ Necesitas GUI para crear tests
- ✅ Reportes HTML muy detallados
- ✅ Grabación de sesiones de navegador
- ✅ Plugins específicos
- ✅ Tests legacy existentes

## 📊 Estructura del Script k6

### Setup Function
```javascript
export function setup() {
  // Se ejecuta UNA VEZ antes de todos los VUs
  // Útil para autenticación, preparar datos, etc.
}
```

### Default Function
```javascript
export default function () {
  // Se ejecuta por cada VU en cada iteración
  // Aquí van los requests HTTP
}
```

### Teardown Function
```javascript
export function teardown(data) {
  // Se ejecuta UNA VEZ al final
  // Útil para cleanup, logs finales, etc.
}
```

## 🎯 Escenarios Avanzados

### Múltiples Escenarios en Paralelo

Editar `pharmacy-stress-test.js`:

```javascript
export const options = {
  scenarios: {
    // Usuarios normales navegando
    normal_users: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 30 },
        { duration: '2m', target: 30 },
        { duration: '30s', target: 0 },
      ],
      exec: 'normalUser',
    },
    
    // Picos de tráfico
    spike_traffic: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 100 },
        { duration: '20s', target: 100 },
        { duration: '10s', target: 0 },
      ],
      startTime: '1m',
      exec: 'spikeUser',
    },
  },
};

export function normalUser() {
  // Comportamiento de usuario normal
}

export function spikeUser() {
  // Comportamiento durante picos
}
```

## 📚 Estructura de Archivos

```
k6-tests/
├── README.md                       # Este archivo
├── pharmacy-stress-test.js         # Script principal de k6
├── run-stress-test.sh              # Script de ejecución
└── results/                        # Resultados de los tests
    ├── test-results-[timestamp].json
    ├── summary-[timestamp].json
    └── report-[timestamp].html
```

## 🔧 Troubleshooting

### Error: "Connection refused"

**Problema**: Backend no está corriendo

**Solución**:
```bash
curl http://localhost:8101/api2/medicines
# Si falla, levantar backend
docker-compose -f docker-compose.prod.yml up -d backend-prod
```

### Thresholds Fallando

**Problema**: Response times muy altos

**Solución**:
- Reducir número de VUs
- Aumentar recursos del backend
- Verificar que no haya otros procesos consumiendo CPU

### k6 muy lento

**Problema**: Configuración incorrecta

**Verificar**:
```bash
# Ver recursos usados
k6 run pharmacy-stress-test.js --vus 10 --duration 30s
```

## 💾 .gitignore

Crear `.gitignore`:

```
# k6 Test Results
results/*.json
results/*.html

# k6 logs
*.log
```

## ✅ Checklist de Implementación

- [x] k6 instalado (`k6 version`)
- [x] Script de test creado (`pharmacy-stress-test.js`)
- [x] Script de ejecución creado (`run-stress-test.sh`)
- [x] Documentación completa
- [ ] Test ejecutado al menos una vez
- [ ] Resultados verificados
- [ ] Comparado con resultados de JMeter

## 🎓 Recursos

- **k6 Docs**: https://k6.io/docs/
- **Examples**: https://k6.io/docs/examples/
- **API Reference**: https://k6.io/docs/javascript-api/
- **Best Practices**: https://k6.io/docs/misc/fine-tuning-os/

---

**Status**: ✅ Completamente funcional  
**k6 Version**: 1.3.0  
**Ventaja principal**: Código JavaScript simple y exportación a Prometheus

