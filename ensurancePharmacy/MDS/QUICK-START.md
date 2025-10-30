# 🚀 OpenObserve - Guía de Inicio Rápido

## ⚡ Setup en 5 Minutos

### 1. Levantar OpenObserve (Vía Jenkins - Recomendado)

```bash
# Ejecutar pipeline en Jenkins para rama master
# Jenkins levantará automáticamente OpenObserve junto con todos los servicios
```

### 2. O Levantar Manualmente

```bash
cd ensurancePharmacy
docker-compose -f docker-compose.prod.yml up -d openobserve
```

### 3. Acceder a OpenObserve

Abrir: **http://localhost:5080**

**Login:**
- Email: `admin@pharmacy.com`  
- Password: `Complexpass#123`

### 4. Configurar Data Source (Una sola vez)

#### Opción A: Usar Prometheus como Data Source

1. En OpenObserve, ir a: **Settings** → **Integrations**
2. Buscar **"Prometheus"**
3. Click en **"Prometheus Remote Read"**
4. Configurar:
   - **URL**: `http://prometheus:9090`
   - **Name**: `Prometheus`
5. Click en **"Test"** y luego **"Save"**

#### Opción B: Scrape Directo de Node Exporter

1. En OpenObserve, ir a: **Metrics** → **Scrape Config**
2. Agregar target:
   - **Job Name**: `node-exporter`
   - **Target**: `node-exporter:9100`
   - **Scrape Interval**: `15s`
3. Save

### 5. Crear Dashboard de Pipeline (5 minutos)

1. **Ir a Dashboards** → **Create New Dashboard**

2. **Configurar Dashboard**:
   - Name: `Pipeline Performance`
   - Description: `CI/CD Pipeline Metrics`
   - Tags: `jenkins`, `pipeline`

3. **Agregar 4 Paneles** (uno por uno):

#### Panel 1: Build Duration
- Click **"+ Add Panel"**
- Title: `Build Duration`
- Query Type: `PromQL`
- Query: 
```promql
default_jenkins_builds_last_build_duration_milliseconds / 1000
```
- Visualization: `Time Series`
- Unit: `seconds`
- Save Panel

#### Panel 2: Success Rate
- Click **"+ Add Panel"**
- Title: `Success Rate`
- Query:
```promql
(1 - (sum(default_jenkins_builds_failed_build_count_total) / sum(default_jenkins_builds_duration_milliseconds_summary_count))) * 100
```
- Visualization: `Gauge`
- Unit: `percent`
- Min: `0`, Max: `100`
- Thresholds: 0-50 (red), 50-80 (yellow), 80-100 (green)
- Save Panel

#### Panel 3: Total Executions
- Click **"+ Add Panel"**
- Title: `Total Pipeline Executions`
- Query:
```promql
default_jenkins_builds_duration_milliseconds_summary_count
```
- Visualization: `Stat` or `Counter`
- Unit: `short`
- Save Panel

#### Panel 4: Queue Size
- Click **"+ Add Panel"**
- Title: `Pipeline Queue Size`
- Query:
```promql
default_jenkins_queue_size_value
```
- Visualization: `Time Series`
- Unit: `short`
- Save Panel

4. **Guardar Dashboard** → Click **"Save"** (arriba derecha)

### 6. Crear Dashboard de Aplicación (5 minutos)

1. **Dashboards** → **Create New Dashboard**
   - Name: `Application Performance`
   - Description: `System and Application Metrics`

2. **Agregar 4 Paneles**:

#### Panel 1: CPU Usage
- Query:
```promql
100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```
- Visualization: `Gauge`
- Unit: `percent`

#### Panel 2: Memory Usage
- Query:
```promql
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
```
- Visualization: `Gauge`
- Unit: `percent`

#### Panel 3: Network Throughput
- Query:
```promql
rate(node_network_receive_bytes_total[1m]) * 8 / 1000000
```
- Visualization: `Time Series`
- Unit: `Mbps`

#### Panel 4: Disk I/O
- Query:
```promql
rate(node_disk_read_bytes_total[1m]) / 1024 / 1024
```
- Visualization: `Time Series`
- Unit: `MB/s`

3. **Guardar Dashboard**

## ✅ ¡Listo!

Ahora tienes:
- ✅ **2 Dashboards en Grafana** (auto-configurados)
- ✅ **2 Dashboards en OpenObserve** (configurados manualmente)
- ✅ **Total: 8 gráficas en cada plataforma**

## 🎯 URLs de Acceso

| Plataforma | URL | Credenciales |
|------------|-----|--------------|
| **Grafana** | http://localhost:3000 | admin / admin |
| **OpenObserve** | http://localhost:5080 | admin@pharmacy.com / Complexpass#123 |
| **Prometheus** | http://localhost:9090 | - |

## 💡 Tips Rápidos

### Ver Métricas sin Dashboard

1. En OpenObserve: **Metrics** → **Query**
2. Escribir query PromQL
3. Click **"Run Query"**
4. Ver resultados

### Explorar Métricas Disponibles

1. **Metrics** → **Metric Explorer**
2. Browse todas las métricas
3. Seleccionar una para ver detalles

### Búsqueda Rápida

En el buscador principal (arriba):
- Buscar: `jenkins`
- Ver todas las métricas de Jenkins
- Click en una para visualizar

## 🎬 Para la Demo

### Mostrar OpenObserve:

1. **Login** en http://localhost:5080
2. **Mostrar Dashboard de Pipeline**:
   - "Aquí vemos las mismas métricas del pipeline"
   - "Pero en una interfaz más moderna"
3. **Mostrar Dashboard de Aplicación**:
   - "Métricas del sistema en tiempo real"
4. **(Bonus) Mostrar Logs**:
   - Ir a **Logs**
   - Mostrar que puede capturar logs también
5. **(Bonus) Comparar con Grafana**:
   - Abrir ambos lado a lado
   - "Ambos muestran las mismas métricas"
   - "OpenObserve es todo-en-uno, Grafana es especializado"

## 🔧 Troubleshooting

### No puedo acceder a http://localhost:5080

```bash
# Verificar que esté corriendo
docker ps | grep openobserve

# Ver logs
docker logs openobserve-prod

# Reiniciar
docker-compose -f docker-compose.prod.yml restart openobserve
```

### No veo métricas

1. Verificar que Prometheus esté UP
2. En OpenObserve:
   - **Settings** → **Integrations**
   - Verificar configuración de Prometheus
3. Cambiar rango de tiempo a "Last 5 minutes"

### Olvidé la contraseña

Contraseña definida en `docker-compose.prod.yml`:
```yaml
ZO_ROOT_USER_PASSWORD=Complexpass#123
```

## 📦 Archivos de Configuración

```
monitoring/openobserve/
├── README-OPENOBSERVE.md          # Documentación completa
├── RESUMEN-OPENOBSERVE.md         # Este archivo
├── QUICK-START.md                 # Guía rápida
├── setup-openobserve.sh           # Script de configuración
├── prometheus-scrape.yaml         # Config de scrape
└── dashboards-openobserve.md      # Queries (auto-generado por script)
```

## 🎯 Checklist de Verificación

- [ ] OpenObserve corriendo: `docker ps | grep openobserve`
- [ ] Acceso a UI: http://localhost:5080
- [ ] Login exitoso
- [ ] Data source Prometheus configurado
- [ ] Dashboard "Pipeline Performance" creado (4 paneles)
- [ ] Dashboard "Application Performance" creado (4 paneles)
- [ ] Métricas mostrándose en paneles
- [ ] Ejecutado al menos 1 build para tener datos

---

**Tiempo estimado de setup**: 15-20 minutos  
**Resultado**: 2 dashboards con 8 gráficas totales  
**Alternativa a**: Prometheus + Grafana (que ya tienes funcionando)

