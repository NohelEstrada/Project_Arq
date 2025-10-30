#!/bin/bash

echo "========================================="
echo "  DIAGNÓSTICO DE NOTIFICACIONES"
echo "========================================="
echo ""

echo "📊 1. ESTADO DE ALERTAS"
echo "========================================"
ALERTS=$(curl -s -u admin:admin http://localhost:3000/api/alertmanager/grafana/api/v2/alerts 2>&1)
ALERT_COUNT=$(echo "$ALERTS" | grep -o '"alertname"' | wc -l | tr -d ' ')
echo "Alertas activas: $ALERT_COUNT"
echo ""
echo "Alertas:"
echo "$ALERTS" | grep -o '"alertname":"[^"]*"' | head -10
echo ""

echo "🔔 2. PROBANDO SLACK"
echo "========================================"
SLACK_URL="https://hooks.slack.com/services/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"
echo "Webhook URL: $SLACK_URL"
echo "Enviando mensaje de prueba..."
SLACK_RESPONSE=$(curl -s -X POST "$SLACK_URL" \
  -H 'Content-Type: application/json' \
  -d '{"text": "🧪 Test de Slack desde diagnóstico"}' \
  -w "%{http_code}" \
  -o /tmp/slack_response.txt 2>&1)

cat /tmp/slack_response.txt
echo ""

if [ "$SLACK_RESPONSE" = "200" ] || grep -q "ok" /tmp/slack_response.txt; then
    echo "✅ Webhook de Slack funciona correctamente"
else
    echo "❌ Webhook de Slack NO funciona (HTTP $SLACK_RESPONSE)"
    echo "   Necesitas regenerar la webhook"
fi
echo ""

echo "📧 3. VERIFICANDO CONFIGURACIÓN EMAIL"
echo "========================================"
EMAIL_CONFIG=$(docker exec grafana-prod env | grep SMTP)
echo "$EMAIL_CONFIG" | sed 's/PASSWORD=.*/PASSWORD=***OCULTA***/g'
echo ""

echo "📋 4. ÚLTIMOS ERRORES DE NOTIFICACIÓN"
echo "========================================"
docker logs grafana-prod --tail 200 2>&1 | grep -i "failed" | grep -i -E "(slack|email|smtp)" | tail -10
echo ""

echo "✅ 5. ÚLTIMAS NOTIFICACIONES EXITOSAS"
echo "========================================"
SUCCESS_COUNT=$(docker logs grafana-prod --tail 200 2>&1 | grep -i "successfully sent" | wc -l | tr -d ' ')
echo "Notificaciones enviadas correctamente: $SUCCESS_COUNT"
if [ "$SUCCESS_COUNT" -gt 0 ]; then
    docker logs grafana-prod --tail 200 2>&1 | grep -i "successfully sent" | tail -5
fi
echo ""

echo "🔍 6. CONTACT POINTS CONFIGURADOS"
echo "========================================"
curl -s -u admin:admin http://localhost:3000/api/v1/provisioning/contact-points 2>&1 | \
  grep -o '"name":"[^"]*"' | head -5
echo ""

echo "========================================="
echo "  RESUMEN"
echo "========================================="
echo ""

if grep -q "ok" /tmp/slack_response.txt; then
    echo "✅ Slack: Webhook funciona"
else
    echo "❌ Slack: Webhook NO funciona - REGENERAR"
fi

if docker logs grafana-prod --tail 50 2>&1 | grep -q "BadCredentials"; then
    echo "❌ Email: Credenciales inválidas - REGENERAR contraseña de aplicación"
else
    echo "⚠️  Email: Verificar credenciales"
fi

if [ "$ALERT_COUNT" -gt 0 ]; then
    echo "✅ Alertas: $ALERT_COUNT alertas activas"
else
    echo "⚠️  Alertas: No hay alertas activas"
fi

echo ""
echo "========================================="
echo "  ACCIONES RECOMENDADAS"
echo "========================================="
echo ""

if ! grep -q "ok" /tmp/slack_response.txt; then
    echo "1. REGENERAR WEBHOOK DE SLACK:"
    echo "   • Ve a: https://api.slack.com/apps"
    echo "   • Selecciona tu app"
    echo "   • Incoming Webhooks → Add New Webhook"
    echo "   • Selecciona canal #alertas"
    echo "   • Actualiza docker-compose.prod.yml con nueva URL"
    echo ""
fi

if docker logs grafana-prod --tail 50 2>&1 | grep -q "BadCredentials"; then
    echo "2. REGENERAR CONTRASEÑA DE APLICACIÓN GMAIL:"
    echo "   • Ve a: https://myaccount.google.com/apppasswords"
    echo "   • Crea nueva contraseña para 'Grafana'"
    echo "   • Actualiza GF_SMTP_PASSWORD en docker-compose.prod.yml"
    echo ""
fi

echo "3. DESPUÉS DE HACER CAMBIOS:"
echo "   docker stop grafana-prod && docker rm grafana-prod"
echo "   docker-compose -f docker-compose.prod.yml up -d grafana"
echo ""

# Limpiar
rm -f /tmp/slack_response.txt

echo "========================================="

