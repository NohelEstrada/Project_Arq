# 📊 Guía para Importar Dashboards en OpenObserve

## Dashboards Disponibles

### 1. **System Metrics Dashboard** (`dashboard-system-metrics.json`)
Métricas del sistema operativo y recursos:
- ✅ CPU Usage %
- ✅ Procesos en Ejecución
- ✅ Uptime del Sistema (segundos)
- ✅ Disco Read (MB/s)
- ✅ Disco Write (MB/s)
- ✅ Espacio de Disco Usado %
- ✅ Network Connections (TCP)
- ✅ Procesos Creados (rate)
- ✅ Disco I/O Operations
- ✅ Memory Usage %
- ✅ Network RX/TX (MB/s)

### 2. **CI/CD Pipeline Metrics Dashboard** (`dashboard-pipeline-metrics.json`)
Métricas del pipeline de Jenkins:
- ✅ Tests Totales (Unit Tests)
- ✅ Tiempo del Último Build
- ✅ Time Wasted in Failed Builds
- ✅ Success Rate %
- ✅ Build Health Score
- ✅ Tests Failing
- ✅ Jenkins Queue Length
- ✅ Executors Status

---

## 🚀 Paso 1: Acceder a OpenObserve

```bash
# OpenObserve está corriendo en:
http://localhost:5080

# Credenciales:
Email: admin@pharmacy.com
Password: Complexpass#123
```

---

## 📥 Paso 2: Configurar Prometheus como Data Source

Antes de importar dashboards, necesitas configurar Prometheus:

### Opción A: Via UI (Recomendado)

1. Abre OpenObserve: http://localhost:5080
2. Inicia sesión con las credenciales
3. Ve a **Settings** → **Data Sources**
4. Click en **Add Data Source**
5. Selecciona **Prometheus**
6. Configura:
   - **Name**: `Prometheus`
   - **URL**: `http://prometheus-prod:9090`
   - **Access**: `Server (default)`
7. Click **Save & Test**

### Opción B: Via Script Automático

```bash
# Ejecutar desde la carpeta del proyecto
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy

# Configurar Prometheus en OpenObserve
./monitoring/openobserve/setup-prometheus-datasource.sh
```

---

## 📊 Paso 3: Importar los Dashboards

### Método 1: Importación Manual via UI

1. Ve a **Dashboards** en el menú lateral
2. Click en **Import**
3. Selecciona el archivo JSON:
   - Para métricas del sistema: `dashboard-system-metrics.json`
   - Para métricas del pipeline: `dashboard-pipeline-metrics.json`
4. Click **Import**
5. Repite para el segundo dashboard

### Método 2: Importación via API (Automático)

```bash
# Importar ambos dashboards automáticamente
./monitoring/openobserve/import-dashboards.sh
```

---

## 🔍 Paso 4: Verificar que las Métricas Funcionan

### Verificación Manual:
1. Abre cada dashboard importado
2. Verifica que los paneles muestren datos
3. Si ves "No Data":
   - Espera 30 segundos (Prometheus recopila cada 15s)
   - Ajusta el rango de tiempo (últimos 15 minutos)
   - Verifica que Prometheus esté activo: http://localhost:9090/targets

### Verificación Automática:
```bash
# Ejecutar script de validación
./monitoring/openobserve/validate-metrics.sh
```

---

## 📝 Queries de Prometheus Utilizadas

### System Metrics:

| Panel | Query PromQL |
|-------|--------------|
| CPU Usage % | `100 - (avg by(instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)` |
| Procesos Running | `node_procs_running` |
| Uptime (segundos) | `time() - node_boot_time_seconds` |
| Disco Read MB/s | `rate(node_disk_read_bytes_total[5m]) / 1024 / 1024` |
| Disco Write MB/s | `rate(node_disk_written_bytes_total[5m]) / 1024 / 1024` |
| Disco Usado % | `100 - ((node_filesystem_avail_bytes * 100) / node_filesystem_size_bytes)` |
| Network Connections | `node_netstat_Tcp_CurrEstab` |
| Procesos Creados | `rate(node_forks_total[5m])` |

### Pipeline Metrics:

| Panel | Query PromQL |
|-------|--------------|
| Tests Totales | `default_jenkins_builds_last_build_tests_total` |
| Último Build (s) | `default_jenkins_builds_last_build_duration_milliseconds / 1000` |
| Success Rate % | `(sum(default_jenkins_builds_success_build_count_total) / sum(default_jenkins_builds_total_build_count_total)) * 100` |
| Time Wasted (h) | `sum(default_jenkins_builds_duration_milliseconds_summary_sum{result="FAILURE"}) / 1000 / 3600` |
| Tests Failing | `default_jenkins_builds_last_build_tests_failing` |

---

## 🔧 Troubleshooting

### Problema: "No Data" en los paneles

**Solución 1**: Verificar que Prometheus está recopilando datos
```bash
curl http://localhost:9090/api/v1/query?query=up
```

**Solución 2**: Verificar targets de Prometheus
```bash
# Ver targets activos
open http://localhost:9090/targets

# Deberías ver:
# ✓ jenkins (up)
# ✓ node-exporter (up)
# ✓ prometheus (up)
```

**Solución 3**: Verificar conexión OpenObserve → Prometheus
```bash
# Desde OpenObserve, test data source
# Settings → Data Sources → Prometheus → Test
```

### Problema: Algunas métricas no aparecen

**Causa**: El job correspondiente no está activo en Prometheus

**Solución**:
```bash
# Ver qué jobs están activos
curl -s http://localhost:9090/api/v1/targets | grep "job"

# Si falta node-exporter:
docker ps | grep node-exporter

# Si falta jenkins:
# Verifica que Jenkins tiene el plugin Prometheus Metrics instalado
```

---

## 📚 Recursos Adicionales

- **OpenObserve Docs**: https://openobserve.ai/docs/
- **Prometheus Query Examples**: http://localhost:9090/graph
- **Node Exporter Metrics**: https://github.com/prometheus/node_exporter
- **Jenkins Metrics Plugin**: https://plugins.jenkins.io/prometheus/

---

## 🆘 Soporte

Si necesitas ayuda:

1. **Verificar estado de servicios**:
   ```bash
   ./monitoring/check-grafana.sh
   ./monitoring/openobserve/validate-metrics.sh
   ```

2. **Ver logs de OpenObserve**:
   ```bash
   docker logs openobserve-prod --tail 50
   ```

3. **Ver logs de Prometheus**:
   ```bash
   docker logs prometheus-prod --tail 50
   ```

