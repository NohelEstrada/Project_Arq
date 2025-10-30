#!/bin/bash

echo "🔍 Verificando conexión Grafana → Prometheus"
echo "=============================================="
echo ""

# Esperar a que Grafana esté listo
echo "⏳ Esperando a que Grafana esté listo..."
sleep 5

# Verificar que Grafana responde
if ! curl -f -s http://localhost:3000/api/health > /dev/null 2>&1; then
    echo "❌ Grafana no está respondiendo"
    exit 1
fi
echo "✅ Grafana está corriendo"
echo ""

# Verificar datasources en Grafana
echo "📊 Verificando datasources configurados:"
DATASOURCES=$(curl -s -u admin:admin http://localhost:3000/api/datasources 2>&1)

if echo "$DATASOURCES" | grep -q "prometheus-prod"; then
    echo "✅ Datasource Prometheus encontrado con URL correcta (prometheus-prod)"
else
    echo "⚠️  Datasource Prometheus puede tener problemas"
    echo "$DATASOURCES" | head -20
fi
echo ""

# Verificar que Prometheus está accesible desde Grafana
echo "🔗 Verificando conectividad Grafana → Prometheus:"
PROXY_TEST=$(curl -s -u admin:admin "http://localhost:3000/api/datasources/proxy/1/api/v1/query?query=up" 2>&1)

if echo "$PROXY_TEST" | grep -q '"status":"success"'; then
    echo "✅ Grafana puede conectarse a Prometheus correctamente"
    echo ""
    echo "Métricas disponibles:"
    echo "$PROXY_TEST" | grep -o '"metric":{[^}]*}' | head -3
else
    echo "❌ Error conectando Grafana → Prometheus"
    echo "Respuesta:"
    echo "$PROXY_TEST" | head -10
fi
echo ""

# Verificar que hay datos en Prometheus
echo "📈 Verificando datos en Prometheus:"
PROM_DATA=$(curl -s "http://localhost:9090/api/v1/query?query=up" 2>&1)

if echo "$PROM_DATA" | grep -q '"status":"success"'; then
    UP_COUNT=$(echo "$PROM_DATA" | grep -o '"value":\[' | wc -l | tr -d ' ')
    echo "✅ Prometheus tiene datos: $UP_COUNT métricas 'up' encontradas"
    echo ""
    echo "Targets activos en Prometheus:"
    echo "$PROM_DATA" | grep -o '"job":"[^"]*"' | sort -u
else
    echo "⚠️  Prometheus puede no tener datos aún"
fi
echo ""

# Resumen
echo "=============================================="
echo "📝 RESUMEN"
echo "=============================================="
echo "Grafana URL: http://localhost:3000"
echo "Prometheus URL: http://localhost:9090"
echo ""
echo "Para ver los dashboards:"
echo "  1. Abre http://localhost:3000"
echo "  2. Usuario: admin / admin"
echo "  3. Ve a Dashboards"
echo "  4. Si aún ves 'No Data', espera 30 segundos para que Prometheus recopile métricas"
echo ""
echo "Para verificar métricas en Prometheus directamente:"
echo "  http://localhost:9090/targets"
echo ""

