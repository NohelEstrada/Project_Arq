# 🚨 Configurar Alertas en OpenObserve

Esta guía te ayudará a configurar las alertas del sistema en OpenObserve usando las variables de plantilla.

## 📋 Alertas Incluidas

### Sistema (Recursos):
1. **CPU Usage Critical** (>90%) - 🔴 Critical
2. **CPU Usage Warning** (>70%) - 🟡 Warning
3. **Memory Usage Critical** (>90%) - 🔴 Critical
4. **Memory Usage Warning** (>80%) - 🟡 Warning
5. **Disk Usage Warning** (>85%) - 🟡 Warning

### Pipeline (Jenkins):
6. **Jenkins Build Failed** - 🔴 Critical
7. **Jenkins Build Duration Long** (>10 min) - 🟡 Warning
8. **Jenkins Error Rate High** (>5%) - 🟡 Warning

## 🔧 Variables de Plantilla Disponibles

### Variables de Alerta:
- `{alert_name}` - Nombre de la alerta
- `{alert_type}` - Tipo de alerta
- `{alert_threshold}` - Valor del umbral
- `{alert_agg_value}` - Valor agregado actual
- `{alert_operator}` - Operador de comparación (>=, >, <, etc.)

### Variables de Tiempo:
- `{alert_trigger_time_str}` - Tiempo de disparo (formato string)
- `{alert_trigger_time}` - Tiempo de disparo
- `{alert_start_time}` - Hora de inicio
- `{alert_end_time}` - Hora de fin

### Variables de Stream:
- `{org_name}` - Nombre de la organización
- `{stream_type}` - Tipo de stream (metrics, logs, traces)
- `{stream_name}` - Nombre del stream

### Otras Variables:
- `{alert_url}` - URL de la alerta
- `{alert_count}` - Contador de alertas
- `{alert_period}` - Período de la alerta

### Limitadores:
- `{rows:N}` - Limitar número de filas
- `{var:N}` - Limitar longitud de string

## 🚀 Cómo Configurar las Alertas

### Opción 1: Interfaz Web (Recomendada)

1. **Acceder a OpenObserve**:
   ```
   http://localhost:5080
   ```
   - Usuario: `root@example.com`
   - Password: `Complexpass#123`

2. **Ir a Alertas**:
   - Click en el menú lateral
   - Selecciona "Alerts"

3. **Crear Nueva Alerta**:
   - Click en "Create Alert"
   
4. **Configurar Alerta**:
   
   **Para CPU Critical**:
   ```
   Nombre: CPU_Usage_Critical
   Stream Type: metrics
   Stream Name: prometheus
   Query Type: PromQL
   Query: 100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
   
   Condition:
   - Column: value
   - Operator: >=
   - Value: 90
   
   Duration: 2 (minutos)
   Frequency: 1 (minutos)
   Time Between Alerts: 30 (minutos)
   ```

5. **Configurar Destinatarios (Slack)**:
   ```json
   {
     "type": "slack",
     "url": "${SLACK_WEBHOOK_URL}",
     "template": {
       "text": "🔴 *ALERTA CRÍTICA: CPU*\n\n*Alerta:* {alert_name}\n*Severidad:* Critical\n*Valor actual:* {alert_agg_value}%\n*Umbral:* {alert_threshold}%\n*Inicio:* {alert_trigger_time_str}\n*URL:* {alert_url}"
     }
   }
   ```

6. **Configurar Destinatarios (Email)**:
   ```json
   {
     "type": "email",
     "recipients": ["dnestrada@unis.edu.gt", "jflores@unis.edu.gt"],
     "template": {
       "subject": "🔴 Alerta Crítica: CPU > 90%",
       "body": "Alerta: {alert_name}\nSeveridad: Critical\nValor actual: {alert_agg_value}%\nUmbral: {alert_threshold}%\nInicio: {alert_trigger_time_str}\n\nEl uso de CPU ha superado el 90% durante más de 2 minutos.\n\nURL: {alert_url}"
     }
   }
   ```

7. **Guardar y Habilitar**:
   - Click en "Save"
   - Asegúrate de que esté "Enabled"

### Opción 2: API (Automatizada)

```bash
# Ir al directorio
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy/monitoring/openobserve

# Crear alerta de CPU Critical
curl -X POST "http://localhost:5080/api/default/alerts" \
  -H "Content-Type: application/json" \
  -u "root@example.com:Complexpass#123" \
  -d '{
    "name": "CPU_Usage_Critical",
    "stream_type": "metrics",
    "stream_name": "prometheus",
    "query_type": "promql",
    "query": "100 - (avg by (instance) (rate(node_cpu_seconds_total{mode=\"idle\"}[5m])) * 100)",
    "condition": {
      "column": "value",
      "operator": ">=",
      "value": 90
    },
    "duration": 2,
    "frequency": 1,
    "time_between_alerts": 30,
    "destination": [
      {
        "type": "slack",
        "url": "${SLACK_WEBHOOK_URL}",
        "template": {
          "text": "🔴 *ALERTA CRÍTICA: CPU*\\n\\n*Alerta:* {alert_name}\\n*Severidad:* Critical\\n*Valor actual:* {alert_agg_value}%\\n*Umbral:* {alert_threshold}%\\n*Inicio:* {alert_trigger_time_str}"
        }
      }
    ],
    "enabled": true,
    "description": "Alerta cuando el uso de CPU supera el 90%"
  }'
```

## 📝 Plantillas para Diferentes Destinos

### Plantilla Slack (Formato Rico):
```json
{
  "text": "🔴 *ALERTA CRÍTICA: {alert_name}*\n\n*Severidad:* Critical\n*Valor:* {alert_agg_value}%\n*Umbral:* {alert_threshold}%\n*Inicio:* {alert_trigger_time_str}\n*Stream:* {stream_name}\n*Org:* {org_name}\n\n<{alert_url}|Ver Detalles>"
}
```

### Plantilla Email (Texto Plano):
```
Subject: 🔴 Alerta: {alert_name}

Alerta: {alert_name}
Severidad: Critical
Organización: {org_name}
Stream: {stream_name}

Valor Actual: {alert_agg_value}%
Umbral: {alert_threshold}%
Operador: {alert_operator}

Inicio: {alert_trigger_time_str}
URL: {alert_url}

Descripción:
El {alert_name} ha superado el umbral configurado.
```

### Plantilla Alert Manager:
```json
[
  {
    "labels": {
      "alertname": "{alert_name}",
      "stream": "{stream_name}",
      "organization": "{org_name}",
      "alerttype": "{alert_type}",
      "severity": "critical"
    },
    "annotations": {
      "summary": "{alert_name} is firing",
      "description": "Value: {alert_agg_value}, Threshold: {alert_threshold}",
      "timestamp": "{alert_trigger_time_str}"
    }
  }
]
```

## 🔍 Queries PromQL por Alerta

### CPU Critical/Warning:
```promql
100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```

### Memory Critical/Warning:
```promql
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
```

### Disk Usage:
```promql
(1 - (node_filesystem_avail_bytes{mountpoint="/tmp"} / node_filesystem_size_bytes{mountpoint="/tmp"})) * 100
```

### Jenkins Build Failed:
```promql
default_jenkins_builds_last_build_result_ordinal
```

### Jenkins Build Duration:
```promql
default_jenkins_builds_last_build_duration_milliseconds / 60000
```

### Jenkins Error Rate:
```promql
(default_jenkins_builds_failed_build_count_total / default_jenkins_builds_total_build_count_total) * 100
```

## ⚙️ Parámetros de Configuración

### Duration (Duración):
- **Critical**: 2 minutos
- **Warning**: 5 minutos
- Define cuánto tiempo debe cumplirse la condición antes de disparar

### Frequency (Frecuencia):
- **1 minuto** para todas las alertas
- Qué tan seguido se evalúa la alerta

### Time Between Alerts:
- **Critical**: 30 minutos
- **Warning**: 240 minutos (4 horas)
- Tiempo mínimo entre envíos de la misma alerta

## ✅ Verificación

Después de configurar, verifica que:

1. ✓ La alerta aparece en la lista de Alerts
2. ✓ El estado es "Enabled"
3. ✓ Los destinatarios están configurados (Slack + Email)
4. ✓ Las queries devuelven datos en el Query Explorer
5. ✓ Prueba disparando una alerta manualmente

## 🧪 Probar Alertas

### Disparar CPU Alert (Para Pruebas):
```bash
# Generar carga de CPU
stress --cpu 4 --timeout 300s
```

### Ver Logs de Alertas:
```bash
# En OpenObserve UI
Logs → Filter by "alert" keyword
```

## 🆘 Solución de Problemas

### Alerta no se dispara:
- Verifica que la query devuelva datos
- Revisa el threshold y el operator
- Confirma que la alerta esté "Enabled"

### No llegan notificaciones:
- Verifica el webhook URL de Slack
- Revisa la configuración de email en OpenObserve
- Checa los logs de OpenObserve

### Variables no se reemplazan:
- Verifica la sintaxis: `{variable}` no `{{variable}}`
- Asegúrate de usar variables existentes
- Revisa que el template esté en formato JSON válido

## 📚 Referencias

- Variables: [Ver lista completa arriba](#-variables-de-plantilla-disponibles)
- Archivo de configuración: `alertas-config.json`
- OpenObserve Docs: https://openobserve.ai/docs/alerts/
- PromQL Guide: https://prometheus.io/docs/prometheus/latest/querying/basics/

## 📞 Contactos

Notificaciones se enviarán a:
- **Email**: dnestrada@unis.edu.gt, jflores@unis.edu.gt
- **Slack**: Canal #alertas

