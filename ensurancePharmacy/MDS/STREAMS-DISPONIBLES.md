# 📊 Streams Disponibles en OpenObserve

OpenObserve crea un stream individual para cada métrica de Prometheus. Aquí están los streams que necesitas para las alertas:

## 🖥️ Sistema (Node Exporter)

### CPU:
- `node_cpu_seconds_total` - Para alertas de CPU

### Memoria:
- `node_memory_MemAvailable_bytes` - Memoria disponible
- `node_memory_MemTotal_bytes` - Memoria total

### Disco:
- `node_filesystem_avail_bytes` - Espacio disponible
- `node_filesystem_size_bytes` - Tamaño total del filesystem
- `node_filesystem_free_bytes` - Espacio libre

### Red:
- `node_network_receive_bytes_total` - Bytes recibidos
- `node_network_transmit_bytes_total` - Bytes transmitidos

## 🔧 Jenkins

### Builds:
- `default_jenkins_builds_last_build_result_ordinal` - Resultado del último build
- `default_jenkins_builds_last_build_duration_milliseconds` - Duración del build
- `default_jenkins_builds_failed_build_count_total` - Total de builds fallidos
- `default_jenkins_builds_success_build_count_total` - Total de builds exitosos
- `default_jenkins_builds_total_build_count_total` - Total de builds
- `default_jenkins_builds_aborted_build_count_total` - Builds abortados

### Tests:
- `default_jenkins_builds_last_build_tests_failing` - Tests fallando
- `default_jenkins_builds_last_build_tests_total` - Tests totales
- `default_jenkins_builds_last_last_build_tests_skipped` - Tests skipped

### Health:
- `default_jenkins_builds_health_score` - Puntuación de salud

### Queue:
- `default_jenkins_executors_available` - Executors disponibles
- `default_jenkins_executors_busy` - Executors ocupados

## 📝 Configuración de Alertas por Stream

### CPU Critical:
```json
{
  "stream_name": "node_cpu_seconds_total",
  "promql": "100 - (avg by (instance) (rate(node_cpu_seconds_total{mode=\"idle\"}[5m])) * 100)"
}
```

### Memory Critical:
```json
{
  "stream_name": "node_memory_MemAvailable_bytes",
  "promql": "(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100"
}
```

### Disk Warning:
```json
{
  "stream_name": "node_filesystem_avail_bytes",
  "promql": "(1 - (node_filesystem_avail_bytes{mountpoint=\"/tmp\"} / node_filesystem_size_bytes{mountpoint=\"/tmp\"})) * 100"
}
```

### Jenkins Build Failed:
```json
{
  "stream_name": "default_jenkins_builds_last_build_result_ordinal",
  "promql": "default_jenkins_builds_last_build_result_ordinal"
}
```

### Jenkins Build Duration:
```json
{
  "stream_name": "default_jenkins_builds_last_build_duration_milliseconds",
  "promql": "default_jenkins_builds_last_build_duration_milliseconds / 60000"
}
```

### Jenkins Error Rate:
```json
{
  "stream_name": "default_jenkins_builds_failed_build_count_total",
  "promql": "(default_jenkins_builds_failed_build_count_total / default_jenkins_builds_total_build_count_total) * 100"
}
```

## 🔍 Cómo Encontrar Streams Disponibles

1. **En OpenObserve UI**:
   - Ve a "Streams" en el menú lateral
   - Selecciona "metrics" como tipo
   - Verás la lista completa de streams disponibles

2. **Filtrar por prefijo**:
   - `node_*` - Métricas del sistema (Node Exporter)
   - `default_jenkins_*` - Métricas de Jenkins
   - `container_*` - Métricas de contenedores Docker

3. **Buscar una métrica específica**:
   - Usa la barra de búsqueda en la sección Streams
   - O filtra por nombre en el Query Explorer

## ⚠️ Nota Importante

En OpenObserve, **cada métrica de Prometheus es un stream separado**. 

Por ejemplo:
- ❌ NO uses: `"stream_name": "prometheus"`
- ✅ SÍ usa: `"stream_name": "node_cpu_seconds_total"`

Esto es diferente de Grafana donde todas las métricas están bajo un solo datasource.

## 💡 Tips

1. **Para queries PromQL complejas**: El `stream_name` debe ser la métrica principal que estás consultando
2. **Para agregaciones**: Usa la métrica base, OpenObserve resolverá las referencias cruzadas
3. **Para múltiples métricas**: Crea alertas separadas para cada métrica o usa la métrica más relevante

## 📋 Resumen de Streams por Alerta

| Alerta | Stream Name |
|--------|-------------|
| CPU Critical | `node_cpu_seconds_total` |
| CPU Warning | `node_cpu_seconds_total` |
| Memory Critical | `node_memory_MemAvailable_bytes` |
| Memory Warning | `node_memory_MemAvailable_bytes` |
| Disk Warning | `node_filesystem_avail_bytes` |
| Jenkins Build Failed | `default_jenkins_builds_last_build_result_ordinal` |
| Jenkins Duration | `default_jenkins_builds_last_build_duration_milliseconds` |
| Jenkins Error Rate | `default_jenkins_builds_failed_build_count_total` |

