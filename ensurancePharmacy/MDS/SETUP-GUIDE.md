# Guía de Configuración - Prometheus y Grafana

## 📋 Checklist de Configuración

### Paso 1: Instalar Plugin de Prometheus en Jenkins

1. Abrir Jenkins: http://localhost:8081
2. Ir a **Manage Jenkins** → **Plugin Manager** → **Available**
3. Buscar: `Prometheus metrics`
4. Instalar el plugin y reiniciar Jenkins
5. Configurar el plugin:
   - Ir a **Manage Jenkins** → **Configure System**
   - Sección **Prometheus**:
     - ✅ Enable collecting path metrics
     - Path: `/prometheus`
6. Verificar que funciona: http://localhost:8081/prometheus

### Paso 2: Iniciar Servicios de Monitoreo

```bash
cd ensurancePharmacy/monitoring
./start-monitoring.sh
```

O manualmente:

```bash
cd ensurancePharmacy
docker-compose -f docker-compose.prod.yml up -d prometheus grafana node-exporter
```

### Paso 3: Acceder a Grafana

1. Abrir: http://localhost:3000
2. Login:
   - Username: `admin`
   - Password: `admin`
3. (Opcional) Cambiar la contraseña cuando lo solicite

### Paso 4: Verificar Dashboards

Ir a **Dashboards** → Verás dos dashboards:

1. **Pipeline Performance Dashboard** (4 gráficas)
2. **Application Performance Dashboard** (4 gráficas)

### Paso 5: Verificar Prometheus Targets

1. Abrir: http://localhost:9090/targets
2. Verificar estado de targets:
   - ✅ prometheus (self) - debe estar UP
   - ✅ node-exporter - debe estar UP
   - ⚠️ jenkins - será DOWN hasta instalar el plugin
   - ⚠️ pharmacy-backend - será DOWN (opcional)

## 🎯 Gráficas Incluidas

### Pipeline Performance (4 gráficas)

1. **Pipeline Build Duration** 
   - Duración de cada build en segundos
   - Muestra: Mean, Last, Max

2. **Pipeline Success Rate**
   - Gauge del 0-100%
   - Verde: >80%, Amarillo: 50-80%, Rojo: <50%

3. **Total Pipeline Executions**
   - Gráfico de barras con total de builds
   - Incrementa con cada ejecución

4. **Pipeline Queue Size**
   - Tamaño actual de la cola de Jenkins
   - Útil para detectar cuellos de botella

### Application Performance (4 gráficas)

1. **CPU Usage**
   - Gauge del 0-100%
   - Verde: <70%, Amarillo: 70-90%, Rojo: >90%

2. **Memory Usage**
   - Gauge del 0-100%
   - Verde: <70%, Amarillo: 70-90%, Rojo: >90%

3. **HTTP Request Rate**
   - Requests por segundo
   - Por endpoint
   - Muestra: Mean, Last, Max

4. **Response Time (95th percentile)**
   - Tiempo de respuesta en ms
   - P95: 95% de requests están por debajo de este tiempo
   - Líneas: <500ms (verde), 500-1000ms (amarillo), >1000ms (rojo)

## 🔧 Troubleshooting

### Jenkins target está DOWN

**Problema**: En http://localhost:9090/targets, jenkins aparece en rojo

**Solución**:
1. Verificar que Jenkins esté corriendo: `curl http://localhost:8081`
2. Verificar que el plugin Prometheus esté instalado
3. Verificar que el endpoint funcione: `curl http://localhost:8081/prometheus`
4. Si usas Mac con Docker Desktop, verifica que `host.docker.internal` funcione

### Grafana no muestra datos

**Problema**: Los dashboards están vacíos

**Solución**:
1. Verificar que Prometheus tenga targets UP: http://localhost:9090/targets
2. Ejecutar algunos builds en Jenkins para generar datos
3. Verificar la conexión del datasource:
   - Grafana → Configuration → Data Sources → Prometheus
   - Click en "Test"
4. Ajustar el rango de tiempo en Grafana (arriba derecha)

### No hay datos históricos

**Problema**: Solo se ven datos desde que se inició Prometheus

**Explicación**: Esto es normal. Prometheus solo almacena datos desde que inicia.

**Solución**: Esperar o ejecutar algunos builds para generar datos.

## 📊 Generar Datos de Prueba

### Ejecutar builds en Jenkins

```bash
# Hacer cambio en código
git add .
git commit -m "test: trigger pipeline"
git push
```

Esto generará:
- ✅ Build duration
- ✅ Success/failure status
- ✅ Queue size
- ✅ Total executions

## 🎨 Personalizar Dashboards

### Editar gráficas existentes

1. Ir al dashboard
2. Click en el título de una gráfica
3. Click en "Edit"
4. Modificar:
   - Query
   - Visualización
   - Colores/thresholds
   - Leyenda
5. Guardar

### Agregar nueva gráfica

1. Ir al dashboard
2. Click en "Add" → "Visualization"
3. Seleccionar Prometheus datasource
4. Escribir query PromQL
5. Configurar visualización
6. Click en "Apply"

### Queries PromQL útiles

```promql
# CPU por núcleo
rate(node_cpu_seconds_total[5m])

# Disco usado
100 - ((node_filesystem_avail_bytes / node_filesystem_size_bytes) * 100)

# Network bandwidth
rate(node_network_receive_bytes_total[5m]) * 8
```

## 📚 Siguientes Pasos

### 1. Configurar Alertas (Opcional)

Crear archivo `monitoring/alerts.yml`:

```yaml
groups:
  - name: pipeline_alerts
    rules:
      - alert: HighFailureRate
        expr: (jenkins_job_failure_count / jenkins_job_count_total) > 0.5
        for: 10m
        labels:
          severity: critical
        annotations:
          summary: "Alta tasa de fallos en pipeline"

      - alert: HighCPU
        expr: node_cpu_usage > 90
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "CPU alto por más de 5 minutos"
```

### 2. Configurar retención de datos

En `prometheus.yml`:

```yaml
global:
  retention.time: 15d  # Guardar datos por 15 días
  retention.size: 10GB # Límite de 10GB
```

### 3. Backup de dashboards

```bash
# Exportar dashboard
curl http://localhost:3000/api/dashboards/uid/pipeline-perf \
  -u admin:admin > backup-pipeline.json
```

## 📞 Recursos Adicionales

- [Prometheus Docs](https://prometheus.io/docs/)
- [Grafana Docs](https://grafana.com/docs/)
- [PromQL Cheat Sheet](https://promlabs.com/promql-cheat-sheet/)
- [Jenkins Prometheus Plugin](https://plugins.jenkins.io/prometheus/)

## ✅ Verificación Final

Checklist antes de presentar:

- [ ] Jenkins Prometheus plugin instalado y funcionando
- [ ] Prometheus UP y targets configurados
- [ ] Grafana accesible en puerto 3000
- [ ] Pipeline Performance Dashboard visible con 4 gráficas
- [ ] Application Performance Dashboard visible con 4 gráficas
- [ ] Al menos 1-2 builds ejecutados para tener datos
- [ ] Screenshots de los dashboards guardados

