#!/bin/bash

echo "🚀 Iniciando servicios de monitoreo para Producción..."
echo ""

# Cambiar al directorio correcto
cd "$(dirname "$0")/.."

# Verificar que docker esté corriendo
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker no está corriendo"
    exit 1
fi

echo "Iniciando Prometheus, Grafana y Node Exporter..."
docker-compose -f docker-compose.prod.yml up -d prometheus grafana node-exporter

echo ""
echo "Esperando a que los servicios inicien..."
sleep 10

echo ""
echo "Servicios de monitoreo iniciados!"
echo ""
echo "URLs de acceso:"
echo "   • Prometheus: http://localhost:9090"
echo "   • Grafana:    http://localhost:3000 (admin/admin)"
echo ""
echo "Dashboards disponibles en Grafana:"
echo "   1. Pipeline Performance Dashboard - Métricas del CI/CD"
echo "   2. Application Performance Dashboard - Métricas de la aplicación"
echo ""
echo "Para ver el estado de los targets de Prometheus:"
echo "   http://localhost:9090/targets"
echo ""
echo "Documentación completa: monitoring/README.md"
echo ""
echo "IMPORTANTE: Para que Jenkins exponga métricas, instala el plugin 'Prometheus Metrics'"
echo "   Jenkins → Manage Jenkins → Plugin Manager → Available → Buscar 'Prometheus metrics'"
echo ""

