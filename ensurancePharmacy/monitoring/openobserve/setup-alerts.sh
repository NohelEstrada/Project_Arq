#!/bin/bash

# Script para configurar alertas en OpenObserve
# Este script crea alertas basadas en el archivo alerts-config.json

echo "🚨 Configuración de Alertas para OpenObserve"
echo "=============================================="
echo ""

# Variables
OPENOBSERVE_URL="http://localhost:5080"
OPENOBSERVE_USER="admin@pharmacy.com"
OPENOBSERVE_PASS="Complexpass#123"
ORG_NAME="default"

echo "📋 Información de Conexión:"
echo "URL: $OPENOBSERVE_URL"
echo "Usuario: $OPENOBSERVE_USER"
echo "Organización: $ORG_NAME"
echo ""

# Verificar que OpenObserve esté corriendo
echo "🔍 Verificando que OpenObserve esté activo..."
if ! curl -s "$OPENOBSERVE_URL/healthz" > /dev/null; then
    echo "❌ Error: OpenObserve no está respondiendo en $OPENOBSERVE_URL"
    echo "Por favor, inicia OpenObserve primero:"
    echo "  docker-compose -f docker-compose.prod.yml up -d openobserve"
    exit 1
fi
echo "✅ OpenObserve está activo"
echo ""

# Función para crear alerta
create_alert() {
    local name="$1"
    local description="$2"
    local query="$3"
    local threshold="$4"
    local duration="$5"
    local severity="$6"
    
    echo "📌 Creando alerta: $name"
    
    # Crear payload JSON
    payload=$(cat <<EOF
{
  "name": "$name",
  "description": "$description",
  "stream": "metrics",
  "query": "$query",
  "condition": {
    "column": "value",
    "operator": ">=",
    "value": $threshold
  },
  "duration": "$duration",
  "frequency": 60,
  "time_between_alerts": 300,
  "destination": [
    {
      "type": "email",
      "recipients": ["dnestrada@unis.edu.gt", "jflores@unis.edu.gt"],
      "template": {
        "subject": "[$severity] $name",
        "body": "$description - Valor: {{value}}"
      }
    },
    {
      "type": "slack",
      "url": "${SLACK_WEBHOOK_URL}",
      "template": {
        "text": "🚨 *$name*\n\n*Severity:* $severity\n*Description:* $description\n*Value:* {{value}}\n*Threshold:* $threshold\n*Time:* {{timestamp}}"
      }
    }
  ],
  "enabled": true
}
EOF
)
    
    # Crear alerta
    response=$(curl -s -X POST "$OPENOBSERVE_URL/api/$ORG_NAME/alerts" \
        -u "$OPENOBSERVE_USER:$OPENOBSERVE_PASS" \
        -H "Content-Type: application/json" \
        -d "$payload")
    
    if echo "$response" | grep -q "error"; then
        echo "  ⚠️  Advertencia: $(echo $response | jq -r '.message // .error')"
    else
        echo "  ✅ Alerta creada exitosamente"
    fi
}

echo "🚀 Creando alertas..."
echo ""

# ALERTAS DE CPU
create_alert \
    "High CPU Usage - Warning" \
    "El uso de CPU ha superado el 70% durante más de 5 minutos" \
    "SELECT avg(100 - (rate(node_cpu_seconds_total{mode='idle'}[5m]) * 100)) as cpu_usage FROM metrics" \
    70 \
    "5m" \
    "WARNING"

create_alert \
    "Critical CPU Usage" \
    "El uso de CPU ha superado el 90% durante más de 2 minutos" \
    "SELECT avg(100 - (rate(node_cpu_seconds_total{mode='idle'}[5m]) * 100)) as cpu_usage FROM metrics" \
    90 \
    "2m" \
    "CRITICAL"

# ALERTAS DE MEMORIA
create_alert \
    "High Memory Usage - Warning" \
    "El uso de memoria ha superado el 80% durante más de 5 minutos" \
    "SELECT avg((1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100) as memory_usage FROM metrics" \
    80 \
    "5m" \
    "WARNING"

create_alert \
    "Critical Memory Usage" \
    "El uso de memoria ha superado el 90% durante más de 2 minutos" \
    "SELECT avg((1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100) as memory_usage FROM metrics" \
    90 \
    "2m" \
    "CRITICAL"

# ALERTAS DE DISCO
create_alert \
    "High Disk Usage" \
    "El uso de disco ha superado el 85%" \
    "SELECT avg((1 - (node_filesystem_avail_bytes{mountpoint='/'} / node_filesystem_size_bytes{mountpoint='/'})) * 100) as disk_usage FROM metrics" \
    85 \
    "5m" \
    "WARNING"

# ALERTAS DE APLICACIÓN
create_alert \
    "High Response Time" \
    "El tiempo de respuesta ha superado 1000ms" \
    "SELECT histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) * 1000 as response_time FROM metrics" \
    1000 \
    "3m" \
    "WARNING"

create_alert \
    "High Error Rate" \
    "La tasa de errores HTTP 5xx ha superado el 5%" \
    "SELECT (rate(http_server_requests_seconds_count{status=~'5..'}[5m]) / rate(http_server_requests_seconds_count[5m])) * 100 as error_rate FROM metrics" \
    5 \
    "3m" \
    "WARNING"

# ALERTAS DE PIPELINE
create_alert \
    "Pipeline Build Failed" \
    "El pipeline de Jenkins ha fallado" \
    "SELECT increase(jenkins_job_failure_total[5m]) as failures FROM metrics" \
    1 \
    "1m" \
    "CRITICAL"

create_alert \
    "Pipeline Taking Too Long" \
    "El pipeline está tardando más de 10 minutos" \
    "SELECT jenkins_job_duration_seconds / 60 as duration_minutes FROM metrics" \
    10 \
    "2m" \
    "WARNING"

create_alert \
    "High Pipeline Queue" \
    "La cola del pipeline tiene más de 5 trabajos pendientes" \
    "SELECT jenkins_queue_size_value as queue_size FROM metrics" \
    5 \
    "5m" \
    "WARNING"

echo ""
echo "✅ Configuración de alertas completada!"
echo ""
echo "📊 Próximos pasos:"
echo "1. Accede a OpenObserve: $OPENOBSERVE_URL"
echo "2. Ve a la sección 'Alerts' para verificar las alertas"
echo "3. Las notificaciones se enviarán a:"
echo "   - Email: dnestrada@unis.edu.gt, jflores@unis.edu.gt"
echo "   - Slack: #unis-project"
echo ""
echo "🧪 Probar alertas:"
echo "Para probar las alertas, puedes ejecutar un stress test:"
echo "  cd ../jmeter-tests"
echo "  ./run-stress-test.sh 100 60 8081"
echo ""
echo "Esto generará carga en el sistema y debería activar las alertas de CPU y memoria."
echo ""

# Mostrar resumen de alertas creadas
echo "📋 Resumen de Alertas Creadas:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🔴 CRÍTICAS (Notificación inmediata):"
echo "  • CPU > 90% por más de 2 minutos"
echo "  • Memoria > 90% por más de 2 minutos"
echo "  • Pipeline fallido"
echo ""
echo "🟡 ADVERTENCIAS (Notificación en 5 minutos):"
echo "  • CPU > 70% por más de 5 minutos"
echo "  • Memoria > 80% por más de 5 minutos"
echo "  • Disco > 85% por más de 5 minutos"
echo "  • Response Time > 1000ms por más de 3 minutos"
echo "  • Error Rate > 5% por más de 3 minutos"
echo "  • Pipeline > 10 minutos de duración"
echo "  • Queue Size > 5 trabajos por más de 5 minutos"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

