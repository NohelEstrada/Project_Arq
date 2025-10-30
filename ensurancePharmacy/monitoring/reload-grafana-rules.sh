#!/bin/bash

# Script para recargar las reglas de alertas de Grafana
# Uso: ./reload-grafana-rules.sh

set -e

GRAFANA_CONTAINER="grafana-prod"
GRAFANA_URL="http://localhost:3000"
GRAFANA_USER="admin"
GRAFANA_PASS="admin"

echo "========================================="
echo "  Recarga de Reglas de Grafana"
echo "========================================="
echo ""

# Verificar que Grafana esté corriendo
if ! docker ps | grep -q "$GRAFANA_CONTAINER"; then
    echo "❌ ERROR: Grafana no está corriendo"
    echo "   Inicia Grafana con: docker-compose -f docker-compose.prod.yml up -d grafana"
    exit 1
fi

echo "✅ Grafana está corriendo"
echo ""

# Opción 1: Intentar recargar vía API
echo "📡 Intentando recargar configuración vía API..."
RELOAD_RESPONSE=$(curl -s -X POST \
    -u "$GRAFANA_USER:$GRAFANA_PASS" \
    "$GRAFANA_URL/api/admin/provisioning/alerting/reload" \
    -w "%{http_code}" \
    -o /tmp/grafana_reload_response.txt)

if [ "$RELOAD_RESPONSE" = "200" ] || [ "$RELOAD_RESPONSE" = "202" ]; then
    echo "✅ Configuración recargada exitosamente vía API"
    echo ""
    echo "📋 Respuesta:"
    cat /tmp/grafana_reload_response.txt
    echo ""
else
    echo "⚠️  No se pudo recargar vía API (HTTP $RELOAD_RESPONSE)"
    echo "   Intentando reiniciar contenedor..."
    echo ""
    
    # Opción 2: Reiniciar contenedor
    echo "🔄 Reiniciando Grafana..."
    docker restart "$GRAFANA_CONTAINER" > /dev/null 2>&1
    
    echo "⏳ Esperando que Grafana inicie (15 segundos)..."
    sleep 15
    
    # Verificar que esté disponible
    MAX_RETRIES=10
    RETRY=0
    while [ $RETRY -lt $MAX_RETRIES ]; do
        if curl -s -o /dev/null -w "%{http_code}" "$GRAFANA_URL/api/health" | grep -q "200"; then
            echo "✅ Grafana reiniciado correctamente"
            break
        fi
        RETRY=$((RETRY + 1))
        if [ $RETRY -lt $MAX_RETRIES ]; then
            echo "   Intento $RETRY/$MAX_RETRIES..."
            sleep 3
        fi
    done
    
    if [ $RETRY -eq $MAX_RETRIES ]; then
        echo "❌ ERROR: Grafana no responde después de reiniciar"
        exit 1
    fi
fi

echo ""
echo "========================================="
echo "  Verificando Reglas de Alertas"
echo "========================================="
echo ""

# Listar reglas activas
RULES_RESPONSE=$(curl -s -u "$GRAFANA_USER:$GRAFANA_PASS" \
    "$GRAFANA_URL/api/ruler/grafana/api/v1/rules")

# Contar reglas
RULES_COUNT=$(echo "$RULES_RESPONSE" | grep -o '"uid"' | wc -l | tr -d ' ')

if [ "$RULES_COUNT" -gt 0 ]; then
    echo "✅ Se encontraron $RULES_COUNT reglas de alerta cargadas"
    echo ""
    
    # Mostrar grupos de reglas
    echo "📂 Grupos de reglas:"
    echo "$RULES_RESPONSE" | grep -o '"name":"[^"]*"' | head -20 | sed 's/"name"://g'
else
    echo "⚠️  No se encontraron reglas de alerta"
    echo "   Verifica el archivo: monitoring/grafana/provisioning/alerting/rules.yml"
fi

echo ""
echo "========================================="
echo "  ✅ Proceso completado"
echo "========================================="
echo ""
echo "🌐 Accede a Grafana:"
echo "   URL: $GRAFANA_URL"
echo "   User: $GRAFANA_USER"
echo "   Pass: $GRAFANA_PASS"
echo ""
echo "📊 Para ver las alertas:"
echo "   $GRAFANA_URL/alerting/list"
echo ""

# Limpiar archivos temporales
rm -f /tmp/grafana_reload_response.txt

