#!/bin/bash

echo "🔍 Diagnóstico de Grafana"
echo "=========================="
echo ""

# Detectar cuál contenedor de Grafana está corriendo
CONTAINER=""
if docker ps | grep -q "grafana-prod"; then
    CONTAINER="grafana-prod"
    ENV="production"
    PORT="3000"
elif docker ps | grep -q "grafana-dev"; then
    CONTAINER="grafana-dev"
    ENV="development"
    PORT="3001"
elif docker ps | grep -q "grafana-uat"; then
    CONTAINER="grafana-uat"
    ENV="uat"
    PORT="3002"
else
    echo "❌ No se encontró ningún contenedor de Grafana corriendo"
    echo ""
    echo "Contenedores Docker activos:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    exit 1
fi

echo "✅ Grafana encontrado: $CONTAINER (ambiente: $ENV)"
echo ""

# Estado del contenedor
echo "📊 Estado del contenedor:"
docker ps --filter "name=$CONTAINER" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

# Verificar si está reiniciándose
RESTART_COUNT=$(docker inspect $CONTAINER --format='{{.RestartCount}}')
echo "🔄 Número de reinicios: $RESTART_COUNT"
echo ""

# Últimos logs
echo "📋 Últimos 30 logs:"
echo "-------------------"
docker logs $CONTAINER --tail 30 2>&1
echo ""

# Verificar conectividad
echo "🌐 Verificando conectividad:"
if curl -f -s http://localhost:$PORT/api/health > /dev/null 2>&1; then
    echo "✅ Grafana responde correctamente en http://localhost:$PORT"
    echo ""
    HEALTH=$(curl -s http://localhost:$PORT/api/health)
    echo "   Respuesta: $HEALTH"
else
    echo "❌ Grafana NO está respondiendo en http://localhost:$PORT"
    echo "   Esperando 5 segundos y reintentando..."
    sleep 5
    if curl -f -s http://localhost:$PORT/api/health > /dev/null 2>&1; then
        echo "✅ Grafana ahora responde correctamente"
    else
        echo "❌ Grafana sigue sin responder"
    fi
fi
echo ""

# Verificar errores en logs
echo "🚨 Buscando errores en logs:"
ERROR_COUNT=$(docker logs $CONTAINER 2>&1 | grep -i "error" | wc -l | tr -d ' ')
if [ "$ERROR_COUNT" -gt 0 ]; then
    echo "⚠️  Se encontraron $ERROR_COUNT líneas con errores"
    echo ""
    echo "Errores principales:"
    docker logs $CONTAINER 2>&1 | grep -i "error" | tail -5
else
    echo "✅ No se encontraron errores recientes"
fi
echo ""

# Verificar archivos de configuración
echo "📁 Verificando archivos de configuración:"
if docker exec $CONTAINER test -f /etc/grafana/provisioning/alerting/alerting.yml; then
    echo "✅ alerting.yml existe"
else
    echo "❌ alerting.yml NO existe"
fi

if docker exec $CONTAINER test -f /etc/grafana/grafana.ini; then
    echo "✅ grafana.ini existe"
else
    echo "❌ grafana.ini NO existe"
fi
echo ""

# Resumen
echo "=========================================="
echo "📝 RESUMEN"
echo "=========================================="
echo "Container: $CONTAINER"
echo "Puerto: $PORT"
echo "URL: http://localhost:$PORT"
echo "Usuario: admin / admin"
echo "Reinicios: $RESTART_COUNT"
echo ""

if [ "$RESTART_COUNT" -gt 5 ]; then
    echo "⚠️  ADVERTENCIA: El contenedor se ha reiniciado muchas veces ($RESTART_COUNT)"
    echo "   Revisa los logs arriba para identificar el problema"
    echo ""
    echo "Soluciones comunes:"
    echo "1. Verifica la configuración de alerting.yml"
    echo "2. Verifica que los volúmenes estén montados correctamente"
    echo "3. Verifica permisos de archivos"
    echo "4. docker-compose down && docker-compose up -d"
fi

echo ""
echo "Para ver logs en tiempo real:"
echo "  docker logs -f $CONTAINER"
echo ""

