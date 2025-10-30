#!/bin/bash

echo "🔍 Validando Métricas Disponibles"
echo "=================================="
echo ""

PROM_URL="http://localhost:9090"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Función para validar una métrica
validate_metric() {
    local METRIC=$1
    local DESCRIPTION=$2
    
    RESULT=$(curl -s "${PROM_URL}/api/v1/query?query=${METRIC}" | grep -o '"status":"success"')
    
    if [ -n "$RESULT" ]; then
        echo -e "${GREEN}✅${NC} $DESCRIPTION"
        return 0
    else
        echo -e "${RED}❌${NC} $DESCRIPTION"
        return 1
    fi
}

echo "📊 Validando Métricas del Sistema:"
echo "-----------------------------------"
validate_metric "node_cpu_seconds_total" "CPU Metrics"
validate_metric "node_procs_running" "Procesos en Ejecución"
validate_metric "node_boot_time_seconds" "Uptime del Sistema"
validate_metric "node_disk_read_bytes_total" "Disco Read"
validate_metric "node_disk_written_bytes_total" "Disco Write"
validate_metric "node_filesystem_avail_bytes" "Espacio de Disco"
validate_metric "node_netstat_Tcp_CurrEstab" "Network Connections"
validate_metric "node_forks_total" "Procesos Creados"
validate_metric "node_memory_MemAvailable_bytes" "Memoria Disponible"
validate_metric "node_network_receive_bytes_total" "Network RX"
validate_metric "node_network_transmit_bytes_total" "Network TX"

echo ""
echo "🏗️  Validando Métricas del Pipeline:"
echo "-------------------------------------"
validate_metric "default_jenkins_builds_last_build_tests_total" "Tests Totales"
validate_metric "default_jenkins_builds_last_build_duration_milliseconds" "Tiempo del Build"
validate_metric "default_jenkins_builds_success_build_count_total" "Builds Exitosos"
validate_metric "default_jenkins_builds_failed_build_count_total" "Builds Fallidos"
validate_metric "default_jenkins_builds_total_build_count_total" "Total Builds"
validate_metric "default_jenkins_builds_health_score" "Build Health Score"
validate_metric "jenkins_queue_size_value" "Jenkins Queue"
validate_metric "jenkins_executor_count_value" "Jenkins Executors"
validate_metric "default_jenkins_uptime" "Jenkins Uptime"

echo ""
echo "=========================================="
echo "📈 Métricas de Ejemplo:"
echo "=========================================="

# Mostrar valores actuales
echo ""
echo "CPU Usage:"
curl -s "${PROM_URL}/api/v1/query?query=100%20-%20(avg%20by(instance)%20(rate(node_cpu_seconds_total{mode=%22idle%22}[5m]))%20*%20100)" | grep -o '"value":\[[^]]*\]' | head -1

echo ""
echo "Procesos Running:"
curl -s "${PROM_URL}/api/v1/query?query=node_procs_running" | grep -o '"value":\[[^]]*\]' | head -1

echo ""
echo "Tests Totales:"
curl -s "${PROM_URL}/api/v1/query?query=default_jenkins_builds_last_build_tests_total" | grep -o '"value":\[[^]]*\]' | head -1

echo ""
echo "Success Rate %:"
curl -s "${PROM_URL}/api/v1/query?query=(sum(default_jenkins_builds_success_build_count_total)%20/%20sum(default_jenkins_builds_total_build_count_total))%20*%20100" | grep -o '"value":\[[^]]*\]' | head -1

echo ""
echo ""
echo "Para ver todas las métricas disponibles:"
echo "  http://localhost:9090/graph"
echo ""

