# Guía: Modificar y Aplicar Reglas de Alertas en Grafana

## 📝 Editar Reglas

Las reglas de alertas se encuentran en:
```
monitoring/grafana/provisioning/alerting/rules.yml
```

### Estructura de una regla:

```yaml
- uid: nombre_unico_regla
  title: "Título descriptivo"
  condition: A  # Condición que debe cumplirse
  data:
    - refId: A
      queryType: promql
      model:
        expr: 'tu_query_prometheus'  # Query PromQL
        intervalMs: 60000
        maxDataPoints: 43200
  for: 5m  # Tiempo que debe persistir antes de alertar
  annotations:
    description: "Descripción de la alerta"
    summary: "Resumen corto"
  labels:
    severity: critical  # critical, warning, info
```

### Severidades disponibles:
- `critical`: Errores críticos que requieren atención inmediata
- `warning`: Advertencias que requieren revisión
- `info`: Información general

## 🔄 Aplicar Cambios

### Opción 1: Script Automático (RECOMENDADO)
```bash
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy
bash monitoring/reload-grafana-rules.sh
```

Este script:
1. Verifica que Grafana esté corriendo
2. Intenta recargar vía API
3. Si falla, reinicia el contenedor
4. Verifica que las reglas se hayan cargado

### Opción 2: Reinicio Manual
```bash
# Reiniciar Grafana
docker restart grafana-prod

# Verificar logs
docker logs -f grafana-prod
```

### Opción 3: Recarga vía API
```bash
curl -X POST http://admin:admin@localhost:3000/api/admin/provisioning/alerting/reload
```

## 🔍 Verificar Alertas

### Ver alertas en Grafana:
```
http://localhost:3000/alerting/list
```

### Ver estado actual de alertas vía API:
```bash
# Ver todas las reglas
curl -s -u admin:admin http://localhost:3000/api/ruler/grafana/api/v1/rules | jq

# Ver alertas activas
curl -s -u admin:admin http://localhost:3000/api/alertmanager/grafana/api/v2/alerts | jq
```

### Verificar logs de Grafana:
```bash
docker logs grafana-prod | grep -i alert
```

## 📊 Ejemplos Comunes

### 1. Alerta de CPU Alto
```yaml
- uid: high_cpu_usage
  title: "CPU Usage Alto"
  condition: A
  data:
    - refId: A
      queryType: promql
      model:
        expr: '100 - (avg by(instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80'
  for: 5m
  annotations:
    description: "El uso de CPU está por encima del 80%"
  labels:
    severity: warning
```

### 2. Alerta de Disco Lleno
```yaml
- uid: disk_space_low
  title: "Espacio en Disco Bajo"
  condition: A
  data:
    - refId: A
      queryType: promql
      model:
        expr: '(node_filesystem_avail_bytes{mountpoint="/"} / node_filesystem_size_bytes{mountpoint="/"}) * 100 < 20'
  for: 10m
  annotations:
    description: "El espacio disponible en disco es menor al 20%"
  labels:
    severity: critical
```

### 3. Alerta de Build Fallido
```yaml
- uid: jenkins_build_failed
  title: "Jenkins Build Fallido"
  condition: A
  data:
    - refId: A
      queryType: promql
      model:
        expr: 'default_jenkins_builds_failed_build_count_total > 0'
  for: 1m
  annotations:
    description: "El último build de Jenkins ha fallado"
  labels:
    severity: warning
```

## 🛠️ Troubleshooting

### Las alertas no se cargan
1. Verifica la sintaxis YAML:
   ```bash
   cat monitoring/grafana/provisioning/alerting/rules.yml | grep -A 10 "uid:"
   ```

2. Revisa los logs de Grafana:
   ```bash
   docker logs grafana-prod --tail 100 | grep -i error
   ```

3. Verifica que el archivo de provisioning esté montado:
   ```bash
   docker exec grafana-prod ls -la /etc/grafana/provisioning/alerting/
   ```

### Las notificaciones no se envían
1. Verifica la configuración de alerting:
   ```bash
   cat monitoring/grafana/provisioning/alerting/alerting.yml
   ```

2. Revisa que el contact point esté configurado correctamente

3. Verifica que el servidor SMTP esté funcionando (para emails)

## 📧 Configurar Notificaciones

Las notificaciones se configuran en:
```
monitoring/grafana/provisioning/alerting/alerting.yml
```

Actualmente configurado:
- **Email**: dnestrada@unis.edu.gt, jflores@unis.edu.gt

Para cambiar destinatarios, edita `alerting.yml` y recarga:
```bash
bash monitoring/reload-grafana-rules.sh
```

## 🎯 Métricas Disponibles

### Sistema (Node Exporter):
- `node_cpu_seconds_total`: Uso de CPU
- `node_memory_*`: Métricas de memoria
- `node_filesystem_*`: Espacio en disco
- `node_network_*`: Tráfico de red
- `node_load1`, `node_load5`, `node_load15`: Load average

### Jenkins:
- `default_jenkins_builds_last_build_*`: Info del último build
- `default_jenkins_builds_success_build_count_total`: Builds exitosos
- `default_jenkins_builds_failed_build_count_total`: Builds fallidos
- `jenkins_executor_*`: Estado de ejecutores
- `jenkins_queue_*`: Cola de builds

### Aplicación (si está instrumentada):
- `app_request_count_total`: Total de requests
- `app_request_latency_seconds`: Latencia de requests

## 📚 Recursos

- [Documentación de Grafana Alerting](https://grafana.com/docs/grafana/latest/alerting/)
- [PromQL Queries](https://prometheus.io/docs/prometheus/latest/querying/basics/)
- [Node Exporter Metrics](https://github.com/prometheus/node_exporter#collectors)

