#!/bin/bash

# Script para probar notificaciones de email
# Simula una alerta para verificar que el email funciona

set -e

GRAFANA_URL="http://localhost:3000"
GRAFANA_USER="admin"
GRAFANA_PASS="admin"

echo "========================================="
echo "  Test de Notificación Email"
echo "========================================="
echo ""

echo "📧 Verificando configuración de contact points..."
CONTACT_POINTS=$(curl -s -u "$GRAFANA_USER:$GRAFANA_PASS" \
    "$GRAFANA_URL/api/v1/provisioning/contact-points")

echo "$CONTACT_POINTS" | grep -q "Email" && echo "✅ Contact point 'Email Notifications' encontrado" || echo "❌ Contact point no encontrado"
echo ""

echo "📨 Enviando notificación de prueba..."
TEST_RESPONSE=$(curl -s -u "$GRAFANA_USER:$GRAFANA_PASS" \
    -X POST "$GRAFANA_URL/api/alertmanager/grafana/api/v2/alerts" \
    -H "Content-Type: application/json" \
    -d '[
      {
        "labels": {
          "alertname": "TestAlert",
          "severity": "warning",
          "component": "test"
        },
        "annotations": {
          "summary": "Alerta de Prueba",
          "description": "Esta es una prueba de notificación por email desde Grafana"
        },
        "startsAt": "2025-10-29T00:00:00Z",
        "endsAt": "2025-10-29T23:59:59Z"
      }
    ]' -w "%{http_code}")

echo "Respuesta HTTP: $TEST_RESPONSE"
echo ""

if echo "$TEST_RESPONSE" | grep -q "200"; then
    echo "✅ Alerta de prueba enviada correctamente"
    echo ""
    echo "📬 Verifica tu bandeja de entrada en:"
    echo "   - dnestrada@unis.edu.gt"
    echo "   - jflores@unis.edu.gt"
    echo ""
    echo "💡 Si no recibes el email en 2-3 minutos, revisa:"
    echo "   1. Carpeta de SPAM"
    echo "   2. Logs de Grafana: docker logs grafana-prod | grep -i smtp"
    echo "   3. Configuración SMTP en docker-compose.prod.yml"
else
    echo "❌ Error al enviar alerta de prueba"
    echo ""
    echo "🔍 Revisar logs:"
    echo "   docker logs grafana-prod --tail 50 | grep -i -E '(smtp|email|notif)'"
fi

echo ""
echo "========================================="
echo "  Alertas Activas Actuales"
echo "========================================="
echo ""

ACTIVE_ALERTS=$(curl -s -u "$GRAFANA_USER:$GRAFANA_PASS" \
    "$GRAFANA_URL/api/alertmanager/grafana/api/v2/alerts")

ALERT_COUNT=$(echo "$ACTIVE_ALERTS" | grep -o '"alertname"' | wc -l | tr -d ' ')

echo "📊 Alertas activas: $ALERT_COUNT"
echo ""

if [ "$ALERT_COUNT" -gt 0 ]; then
    echo "Alertas:"
    echo "$ACTIVE_ALERTS" | grep -o '"alertname":"[^"]*"' | head -10
fi

echo ""

