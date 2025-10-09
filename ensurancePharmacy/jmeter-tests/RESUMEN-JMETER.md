# 📊 Resumen - Tests de Stress con Apache JMeter

## ✅ Implementación Completada

### 🛠️ Software Instalado

- ✅ **Apache JMeter 5.6.3** instalado via Homebrew
- ✅ Ubicación: `/opt/homebrew/bin/jmeter`

### 📁 Archivos Creados

```
jmeter-tests/
├── pharmacy-stress-test.jmx        ← Test Plan de JMeter
├── run-stress-test.sh              ← Script de ejecución (chmod +x)
├── README.md                       ← Documentación completa
├── RESUMEN-JMETER.md              ← Este archivo
├── .gitignore                      ← Ignora resultados de tests
└── results/                        ← Directorio para resultados
    └── .gitkeep                    ← Mantiene directorio en Git
```

## 🎯 Configuración del Test Plan

### Test de Stress Configurado

**Parámetros por defecto:**
- 👥 **50 usuarios concurrentes**
- ⏱️ **30 segundos de ramp-up** (aumenta gradualmente)
- 🔄 **10 loops por usuario** (cada usuario repite 10 veces)
- 📊 **~2,000 requests totales** (50 × 10 × 4 endpoints)

**Endpoints probados:**
1. `GET /api2/medicines` - Listar medicinas
2. `GET /api2/categories` - Listar categorías
3. `GET /api2/prescriptions` - Listar prescripciones
4. `GET /api2/medicines/1` - Detalle de medicina

**Características:**
- ✅ Think time aleatorio (1±0.5s entre requests)
- ✅ Assertions de HTTP 200
- ✅ Keep-alive habilitado
- ✅ Resultados en JTL, CSV y HTML

## 🚀 Cómo Usar

### Ejecución Básica (50 usuarios)

```bash
cd ensurancePharmacy/jmeter-tests
./run-stress-test.sh
```

### Ejecución Personalizada

```bash
# Sintaxis:
./run-stress-test.sh [usuarios] [ramp-time] [puerto]

# Ejemplos:
./run-stress-test.sh 100 60 8101    # Test pesado
./run-stress-test.sh 25 15 8084     # Test ligero en DEV
./run-stress-test.sh 200 120 8101   # Test extremo
```

### Ver Resultados

```bash
# El script automáticamente te dirá la ubicación del reporte
open results/html-report-[timestamp]/index.html
```

## 📈 Reportes Generados

### 1. Reporte HTML Interactivo

**Ubicación**: `results/html-report-[timestamp]/index.html`

**Contiene:**
- 📊 **Dashboard**: Vista general del test
  - APDEX score
  - Total requests
  - Error rate
  - Response times

- 📉 **Charts**: Gráficas de performance
  - Response times over time
  - Active threads over time
  - Throughput over time
  - Response times percentiles

- 📋 **Statistics Table**: Por cada endpoint
  - Samples count
  - Error %
  - Average/Min/Max response time
  - 90th/95th/99th percentiles
  - Throughput
  - Received/Sent KB/sec

- ❌ **Errors**: Detalles de errores (si los hay)

### 2. Archivo JTL (Datos Crudos)

**Ubicación**: `results/test-results-[timestamp].jtl`

Datos detallados de cada request:
- Timestamp
- Elapsed time
- Response code
- Success/Failure
- Latency
- URL
- Thread name

### 3. CSV Agregado

**Ubicación**: `results/aggregate-report.csv`

Resumen por endpoint para análisis en Excel.

## 🎭 Escenarios de Prueba Recomendados

### Desarrollo (DEV - Puerto 8084)
```bash
./run-stress-test.sh 10 10 8084
```
- **Objetivo**: Verificar funcionalidad básica
- **Usuarios**: 10
- **Duración**: ~2 minutos

### Pre-producción (UAT - Puerto 8091)
```bash
./run-stress-test.sh 50 30 8091
```
- **Objetivo**: Prueba de carga normal
- **Usuarios**: 50
- **Duración**: ~6 minutos

### Producción (PROD - Puerto 8101)
```bash
./run-stress-test.sh 100 60 8101
```
- **Objetivo**: Prueba de carga alta
- **Usuarios**: 100
- **Duración**: ~12 minutos

### Prueba de Límites
```bash
./run-stress-test.sh 200 120 8101
```
- **Objetivo**: Encontrar el límite del servidor
- **Usuarios**: 200
- **Duración**: ~25 minutos

## 📊 Integración con Grafana

**Durante el test de stress**, puedes ver en tiempo real en Grafana:

1. **Application Performance Dashboard** (http://localhost:3000)
   - 📈 CPU Usage aumentará
   - 📈 Memory Usage aumentará
   - 📈 Network Throughput aumentará
   - 📈 Disk I/O aumentará

2. **Correlación**:
   - Ver cuánto CPU/Memory consume cada nivel de carga
   - Identificar cuellos de botella
   - Detectar límites del servidor

## 🎯 Criterios de Éxito

| Métrica | Verde | Amarillo | Rojo |
|---------|-------|----------|------|
| Avg Response Time | <500ms | 500-1000ms | >1000ms |
| 95th Percentile | <1000ms | 1000-2000ms | >2000ms |
| Error Rate | 0% | <1% | >1% |
| Throughput | >50 req/s | 20-50 req/s | <20 req/s |

## 🔄 Workflow Recomendado

1. **Levantar aplicación**:
   ```bash
   # Ejecutar pipeline en Jenkins o:
   cd ensurancePharmacy
   docker-compose -f docker-compose.prod.yml up -d
   ```

2. **Abrir Grafana** (para monitoreo en tiempo real):
   ```bash
   open http://localhost:3000
   ```

3. **Ejecutar stress test**:
   ```bash
   cd jmeter-tests
   ./run-stress-test.sh
   ```

4. **Analizar resultados**:
   - Ver reporte HTML de JMeter
   - Ver gráficas en Grafana
   - Documentar hallazgos

## 📝 Ejemplo de Uso Completo

```bash
# 1. Levantar servicios (vía Jenkins o docker-compose)
# 2. Ir al directorio de tests
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy/jmeter-tests

# 3. Ejecutar test
./run-stress-test.sh 50 30 8101

# Output esperado:
# ========================================
#   Pharmacy Stress Test - Apache JMeter
# ========================================
# 
# Configuración del test:
#   Usuarios concurrentes: 50
#   Ramp-up time: 30 segundos
#   Puerto backend: 8101
#   Loops por usuario: 10
# 
# Backend activo en puerto 8101
# 
# Ejecutando test de stress...
# ...
# Test completado exitosamente!
# 
# Resultados guardados en:
#   - HTML Report: results/html-report-20251008-001234/index.html
# 
# Resumen rápido:
#   Total de requests: 2000
#   Requests exitosos: 2000
#   Success rate: 100.00%

# 4. Ver reporte
open results/html-report-[timestamp]/index.html
```

## 🎓 Para la Demostración

### Preparación (antes de la demo):

1. ✅ Asegurar que el backend esté corriendo
2. ✅ Tener Grafana abierto (http://localhost:3000)
3. ✅ Preparar el comando: `./run-stress-test.sh 50 30 8101`

### Durante la demo:

1. **Mostrar el test plan**:
   - "Aquí está configurado JMeter para simular 50 usuarios"
   - "Cada usuario hace 10 requests a 4 endpoints diferentes"

2. **Ejecutar el test**:
   - `./run-stress-test.sh`
   - "El test tardará aproximadamente 6 minutos"

3. **Mostrar Grafana en tiempo real**:
   - Cambiar a Grafana
   - "Aquí vemos cómo aumenta el CPU y la memoria mientras corre el test"
   - "El network throughput muestra la carga de red"

4. **Mostrar resultados HTML**:
   - Abrir el reporte HTML
   - "Aquí vemos que procesó 2000 requests con éxito"
   - "El tiempo de respuesta promedio es X ms"
   - "El percentil 95 es Y ms"

## 🎁 Bonus - Test Plan Alternativo

Si quieres crear un test más avanzado:

```bash
# Abrir JMeter GUI
jmeter -t pharmacy-stress-test.jmx

# Puedes agregar:
# - CSV Data Set para datos dinámicos
# - Más endpoints (POST, PUT, DELETE)
# - Autenticación
# - Cookies
# - Headers personalizados
```

---

**Status**: ✅ Completamente funcional y listo para usar
**JMeter Version**: 5.6.3
**Platform**: macOS (arm64)

