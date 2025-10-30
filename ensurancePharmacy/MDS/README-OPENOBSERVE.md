# 🔍 OpenObserve - Plataforma de Observabilidad

## 📋 ¿Qué es OpenObserve?

OpenObserve es una plataforma moderna de observabilidad todo-en-uno que combina:
- 📊 **Métricas** (como Prometheus)
- 📝 **Logs** (como Elasticsearch)
- 🔗 **Trazas** (como Jaeger)

**Ventajas sobre Prometheus + Grafana:**
- ✅ Todo integrado en una sola interfaz
- ✅ Más ligero en recursos (usa menos memoria)
- ✅ Interfaz más moderna e intuitiva
- ✅ Búsqueda full-text en logs y métricas
- ✅ Compatible con formato Prometheus

## 🚀 Inicio Rápido

### 1. Levantar OpenObserve

OpenObserve se levanta automáticamente con el docker-compose:

```bash
cd ensurancePharmacy
docker-compose -f docker-compose.prod.yml up -d openobserve
```

O si quieres levantar todo:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### 2. Acceder a OpenObserve

**URL**: http://localhost:5080

**Credenciales:**
- Email: `admin@pharmacy.com`
- Password: `Complexpass#123`

### 3. Configurar Data Sources

Ejecutar el script de configuración:

```bash
cd monitoring/openobserve
./setup-openobserve.sh
```

O configurar manualmente siguiendo las instrucciones del script.

## 📊 Dashboards a Crear

### Dashboard 1: Pipeline Performance (4 Paneles)

#### 1. Build Duration
- **Visualization**: Line Chart
- **Query**:
```promql
default_jenkins_builds_last_build_duration_milliseconds / 1000
```
- **Description**: Duración de cada build en segundos

#### 2. Success Rate
- **Visualization**: Gauge
- **Query**:
```promql
(1 - (sum(default_jenkins_builds_failed_build_count_total) / sum(default_jenkins_builds_duration_milliseconds_summary_count))) * 100
```
- **Description**: Porcentaje de builds exitosos
- **Range**: 0-100%
- **Thresholds**: 
  - 0-50: Red
  - 50-80: Yellow
  - 80-100: Green

#### 3. Total Pipeline Executions
- **Visualization**: Counter/Stat
- **Query**:
```promql
default_jenkins_builds_duration_milliseconds_summary_count
```
- **Description**: Número total de builds ejecutados

#### 4. Pipeline Queue Size
- **Visualization**: Line Chart
- **Query**:
```promql
default_jenkins_queue_size_value
```
- **Description**: Builds en cola esperando ejecución

---

### Dashboard 2: Application Performance (4 Paneles)

#### 1. CPU Usage
- **Visualization**: Gauge
- **Query**:
```promql
100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```
- **Description**: Uso de CPU del servidor
- **Range**: 0-100%
- **Thresholds**:
  - 0-70: Green
  - 70-90: Yellow
  - 90-100: Red

#### 2. Memory Usage
- **Visualization**: Gauge
- **Query**:
```promql
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
```
- **Description**: Uso de memoria RAM
- **Range**: 0-100%
- **Thresholds**:
  - 0-70: Green
  - 70-90: Yellow
  - 90-100: Red

#### 3. Network Throughput
- **Visualization**: Area Chart
- **Query**:
```promql
rate(node_network_receive_bytes_total[1m]) * 8 / 1000000
```
- **Description**: Velocidad de red en Mbps
- **Legend**: {{device}} - Received

#### 4. Disk I/O Read Rate
- **Visualization**: Line Chart
- **Query**:
```promql
rate(node_disk_read_bytes_total[1m]) / 1024 / 1024
```
- **Description**: Velocidad de lectura del disco en MB/s
- **Legend**: {{device}} - Read

## 🎨 Configuración Manual en OpenObserve

### Paso 1: Crear Organización

1. Login en http://localhost:5080
2. Si es primera vez, crear organización "pharmacy"

### Paso 2: Agregar Data Source Prometheus

1. Ir a **Settings** → **Integrations**
2. Seleccionar **Prometheus Remote Write**
3. Configurar:
   - **Organization**: pharmacy
   - **Stream Name**: metrics
   - **Remote Write URL**: Copiar y guardar

4. O agregar como scrape target:
   - Ir a **Metrics** → **Scrape Targets**
   - Agregar los jobs del archivo `prometheus-scrape.yaml`

### Paso 3: Crear Dashboard de Pipeline

1. Ir a **Dashboards** → **Create Dashboard**
2. Nombre: **"Pipeline Performance"**
3. Description: **"Métricas del CI/CD Pipeline"**
4. Tags: `jenkins`, `pipeline`, `ci-cd`

5. Agregar cada panel:
   - Click en **"Add Panel"**
   - Seleccionar **"Metrics"**
   - Ingresar query de Prometheus
   - Configurar visualización
   - Guardar panel

6. Repetir para los 4 paneles del pipeline

### Paso 4: Crear Dashboard de Aplicación

1. Ir a **Dashboards** → **Create Dashboard**
2. Nombre: **"Application Performance"**
3. Description: **"Métricas de la Aplicación y Sistema"**
4. Tags: `application`, `system`, `performance`

5. Agregar cada panel (4 en total)
6. Guardar dashboard

## 🔧 Configuración Avanzada

### Habilitar Remote Write desde Prometheus a OpenObserve

Si quieres que Prometheus envíe datos a OpenObserve automáticamente:

1. Obtener la URL de Remote Write de OpenObserve
2. Editar `monitoring/prometheus.yml`:

```yaml
remote_write:
  - url: "http://openobserve:5080/api/pharmacy/prometheus/api/v1/write"
    basic_auth:
      username: "admin@pharmacy.com"
      password: "Complexpass#123"
```

3. Reiniciar Prometheus:
```bash
docker-compose -f docker-compose.prod.yml restart prometheus
```

## 📈 Ventajas de OpenObserve

### vs Prometheus + Grafana:

| Característica | Prometheus+Grafana | OpenObserve |
|----------------|-------------------|-------------|
| Componentes | 2 separados | 1 integrado |
| Uso de RAM | ~500MB + ~200MB | ~300MB total |
| Interfaz | Separadas | Única e integrada |
| Logs | Necesita Loki | ✅ Incluido |
| Trazas | Necesita Jaeger | ✅ Incluido |
| Búsqueda | Básica | Full-text avanzada |
| Setup | Más complejo | Más simple |

### Características Únicas:

- 🔍 **Búsqueda avanzada**: Full-text en todas las métricas
- 📊 **SQL queries**: Consultar métricas con SQL
- 🎯 **Alertas integradas**: Sin necesidad de Alertmanager
- 📱 **UI moderna**: Interfaz más intuitiva
- 💾 **Storage eficiente**: Usa menos disco

## 🎯 Uso Básico

### Ver Métricas en Tiempo Real

1. Ir a **Metrics** en el menú lateral
2. Seleccionar organización: **pharmacy**
3. En el query builder, escribir:
```promql
node_cpu_seconds_total
```
4. Click en **Run Query**
5. Ver gráfica en tiempo real

### Crear Panel Rápido

1. Ir a **Dashboards**
2. Click en dashboard existente o crear nuevo
3. **Add Panel**
4. Configurar:
   - **Title**: Nombre del panel
   - **Query**: Query de Prometheus
   - **Visualization**: Line, Gauge, Bar, etc.
   - **Unit**: seconds, percent, bytes, etc.
5. **Save**

### Explorar Logs (Bonus)

OpenObserve también puede capturar logs de los contenedores:

1. Ir a **Logs** en el menú
2. Filtrar por: `service: backend` o `service: jenkins`
3. Ver logs en tiempo real
4. Buscar errores o patrones específicos

## 🔗 Integración con Prometheus

OpenObserve puede trabajar de 3 maneras:

### Opción 1: Como Visualizador (Actual)
- Prometheus recolecta métricas
- OpenObserve lee de Prometheus
- Ambos funcionan en paralelo

### Opción 2: Como Reemplazo
- Desactivar Grafana
- OpenObserve reemplaza a Grafana
- Prometheus sigue recolectando

### Opción 3: Completo
- OpenObserve reemplaza ambos
- Desactivar Prometheus y Grafana
- OpenObserve hace todo (más simple)

## 📚 Queries Útiles en OpenObserve

### Métricas de Sistema

```promql
# CPU por core
rate(node_cpu_seconds_total[5m])

# Memoria disponible en GB
node_memory_MemAvailable_bytes / 1024 / 1024 / 1024

# Disco usado en %
100 - ((node_filesystem_avail_bytes / node_filesystem_size_bytes) * 100)

# Network bandwidth en Mbps
rate(node_network_receive_bytes_total[5m]) * 8 / 1000000
```

### Métricas de Jenkins

```promql
# Última duración de build en minutos
default_jenkins_builds_last_build_duration_milliseconds / 1000 / 60

# Health score
default_jenkins_builds_health_score

# Tests fallidos en último build
default_jenkins_builds_last_build_tests_failing

# Total de tests
default_jenkins_builds_last_build_tests_total
```

### Búsqueda con SQL (Único de OpenObserve)

OpenObserve permite queries SQL sobre las métricas:

```sql
SELECT 
  time,
  value,
  labels
FROM metrics
WHERE 
  __name__ = 'node_cpu_seconds_total'
  AND time > now() - interval '1 hour'
ORDER BY time DESC
LIMIT 100
```

## 🎬 Demo con OpenObserve

### Preparación:

1. Levantar servicios:
```bash
cd ensurancePharmacy
docker-compose -f docker-compose.prod.yml up -d
```

2. Abrir OpenObserve:
```bash
open http://localhost:5080
```

3. Login con credenciales

### Durante la Demo:

1. **Mostrar Dashboard de Pipeline**:
   - "Aquí vemos 4 métricas clave del pipeline CI/CD"
   - Explicar cada panel

2. **Mostrar Dashboard de Aplicación**:
   - "Estas son las métricas del sistema en producción"
   - Mostrar gauges de CPU y Memory en tiempo real

3. **Explorar Métricas**:
   - Ir a sección "Metrics"
   - Mostrar búsqueda de métricas
   - Ejecutar query en vivo

4. **Ejecutar Stress Test** (mientras se muestra OpenObserve):
```bash
cd jmeter-tests
./run-stress-test.sh 50 30 8101
```
   - Ver cómo suben las métricas en tiempo real

5. **Comparar con Grafana** (si quieres):
   - Abrir ambos lado a lado
   - Mostrar que muestran los mismos datos
   - Destacar ventajas de OpenObserve

## 🔧 Troubleshooting

### OpenObserve no inicia

```bash
# Ver logs
docker logs openobserve-prod

# Reiniciar
docker-compose -f docker-compose.prod.yml restart openobserve
```

### No ve métricas de Prometheus

1. Verificar que Prometheus esté UP:
   - http://localhost:9090/targets

2. En OpenObserve:
   - Settings → Integrations
   - Verificar que Prometheus esté configurado
   - Test connection

### Dashboards vacíos

1. Verificar que las métricas existan:
   - Ir a Metrics → Query Builder
   - Buscar `node_cpu_seconds_total`
   - Si no aparece, configurar data source

2. Verificar rango de tiempo:
   - Cambiar a "Last 5 minutes"
   - Refresh

## 📊 Comparación: OpenObserve vs Grafana

### Ambos en Paralelo (Actual)

Puedes usar ambos simultáneamente:
- **Grafana**: http://localhost:3000 (dashboards pre-configurados)
- **OpenObserve**: http://localhost:5080 (más funcionalidades)

### ¿Cuál usar para la demo?

**OpenObserve**:
- ✅ Más moderno
- ✅ Todo en uno
- ✅ Logs incluidos
- ⚠️ Requiere configuración manual de dashboards

**Grafana**:
- ✅ Dashboards ya configurados y funcionando
- ✅ Auto-provisioning
- ✅ Más maduro y conocido
- ⚠️ Solo métricas (no logs)

**Recomendación**: Muestra **ambos** para demostrar versatilidad:
1. Grafana: Para mostrar dashboards listos y funcionales
2. OpenObserve: Para mostrar plataforma moderna todo-en-uno

## 🎯 Configuración de Dashboards en OpenObserve

### Pasos Detallados:

#### 1. Crear Dashboard de Pipeline

1. Login en http://localhost:5080
2. Ir a **Dashboards** en menú lateral
3. Click en **"+ New Dashboard"**
4. Configurar:
   - Name: `Pipeline Performance`
   - Description: `CI/CD Pipeline Metrics`
5. Click en **"Create"**

#### 2. Agregar Panel 1 (Build Duration)

1. Click en **"Add Panel"**
2. Configurar:
   - **Title**: Build Duration
   - **Query Type**: PromQL
   - **Query**: `default_jenkins_builds_last_build_duration_milliseconds / 1000`
   - **Visualization**: Time Series
   - **Unit**: seconds
3. Click en **"Save"**

#### 3. Agregar Panel 2 (Success Rate)

1. Click en **"Add Panel"**
2. Configurar:
   - **Title**: Success Rate
   - **Query**: `(1 - (sum(default_jenkins_builds_failed_build_count_total) / sum(default_jenkins_builds_duration_milliseconds_summary_count))) * 100`
   - **Visualization**: Gauge
   - **Unit**: percent
   - **Min**: 0, **Max**: 100
   - **Thresholds**:
     - 0-50: Red
     - 50-80: Yellow  
     - 80-100: Green
3. Click en **"Save"**

#### 4. Agregar Panel 3 (Total Executions)

1. Click en **"Add Panel"**
2. Configurar:
   - **Title**: Total Pipeline Executions
   - **Query**: `default_jenkins_builds_duration_milliseconds_summary_count`
   - **Visualization**: Stat/Counter
   - **Unit**: short
3. Click en **"Save"**

#### 5. Agregar Panel 4 (Queue Size)

1. Click en **"Add Panel"**
2. Configurar:
   - **Title**: Pipeline Queue Size
   - **Query**: `default_jenkins_queue_size_value`
   - **Visualization**: Time Series
   - **Unit**: short
3. Click en **"Save"**

#### 6. Guardar Dashboard

1. Click en **"Save Dashboard"** (arriba derecha)
2. Dashboard de Pipeline completo ✅

---

#### Repetir para Dashboard de Aplicación

Crear segundo dashboard con los 4 paneles de aplicación usando las queries de la sección anterior.

## 🌟 Características Adicionales de OpenObserve

### 1. Logs de Aplicación

Puedes enviar logs a OpenObserve:

```bash
# Ejemplo: Ver logs de contenedores
docker logs backend-prod | curl -X POST \
  -u admin@pharmacy.com:Complexpass#123 \
  http://localhost:5080/api/pharmacy/_json \
  -d @-
```

### 2. Alertas

Crear alertas directamente en OpenObserve:

1. Ir a **Alerts**
2. **Create Alert**
3. Configurar:
   - Query: `node_cpu_usage > 90`
   - Condition: Para > 5 minutos
   - Notification: Email/Webhook

### 3. SQL Queries

Explorar métricas con SQL:

```sql
SELECT * FROM metrics
WHERE __name__ = 'node_memory_MemAvailable_bytes'
AND time > now() - interval '1 hour'
```

### 4. Búsqueda Full-Text

Buscar cualquier métrica por nombre o etiquetas:
- Buscar: "jenkins"
- Ver todas las métricas relacionadas con Jenkins

## 📖 Estructura de Archivos

```
monitoring/openobserve/
├── README-OPENOBSERVE.md          # Este archivo
├── setup-openobserve.sh           # Script de configuración
├── prometheus-scrape.yaml         # Config de scrape
└── dashboards-openobserve.md      # Queries para dashboards (auto-generado)
```

## 🚀 Comandos Útiles

```bash
# Iniciar OpenObserve
docker-compose -f docker-compose.prod.yml up -d openobserve

# Ver logs
docker logs openobserve-prod -f

# Reiniciar
docker-compose -f docker-compose.prod.yml restart openobserve

# Detener
docker-compose -f docker-compose.prod.yml stop openobserve

# Ver datos almacenados
docker exec openobserve-prod ls -lh /data
```

## 🎓 Para la Demostración

### Opción A: Mostrar Solo OpenObserve

1. Abrir http://localhost:5080
2. Login
3. Mostrar Dashboard de Pipeline (4 paneles)
4. Mostrar Dashboard de Aplicación (4 paneles)
5. Ejecutar stress test y mostrar métricas en tiempo real
6. (Bonus) Mostrar búsqueda de métricas o logs

### Opción B: Comparar OpenObserve vs Grafana

1. Abrir ambos lado a lado:
   - Grafana: http://localhost:3000
   - OpenObserve: http://localhost:5080

2. Mostrar que ambos muestran las mismas métricas:
   - "Grafana es más tradicional y ampliamente usado"
   - "OpenObserve es más moderno y todo-en-uno"

3. Destacar ventajas de OpenObserve:
   - Interface más moderna
   - Logs incluidos
   - Búsqueda avanzada
   - Menor uso de recursos

## ✅ Checklist de Setup

- [ ] OpenObserve levantado (`docker ps | grep openobserve`)
- [ ] Acceso a UI (http://localhost:5080)
- [ ] Login exitoso con credenciales
- [ ] Data source Prometheus configurado
- [ ] Dashboard de Pipeline creado (4 paneles)
- [ ] Dashboard de Aplicación creado (4 paneles)
- [ ] Al menos un build ejecutado (para tener datos)
- [ ] Verificar que los paneles muestren datos

## 📞 Recursos

- **OpenObserve Docs**: https://openobserve.ai/docs/
- **Prometheus Compatibility**: https://openobserve.ai/docs/ingestion/prometheus/
- **Dashboard Examples**: https://openobserve.ai/docs/user-guide/dashboards/

---

**Nota**: OpenObserve y Grafana pueden coexistir. Puedes usar ambos según tus necesidades o preferencias.

**Puerto**: 5080  
**Credenciales**: admin@pharmacy.com / Complexpass#123  
**Data Storage**: Docker volume `openobserve-data`

