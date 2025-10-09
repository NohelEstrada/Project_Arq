#!/bin/bash

# Script para configurar OpenObserve con dashboards y data sources
# Se ejecuta después de que OpenObserve esté corriendo

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "========================================"
echo "  OpenObserve Configuration Script"
echo "========================================"
echo ""

# Variables
OPENOBSERVE_URL="http://localhost:5080"
ORG_NAME="pharmacy"
EMAIL="admin@pharmacy.com"
PASSWORD="Complexpass#123"

# Esperar a que OpenObserve esté listo
echo "Esperando a que OpenObserve esté disponible..."
for i in {1..30}; do
    if curl -s ${OPENOBSERVE_URL} > /dev/null 2>&1; then
        echo -e "${GREEN}OpenObserve está disponible!${NC}"
        break
    fi
    echo -n "."
    sleep 2
done
echo ""

# Obtener token de autenticación
echo "Autenticando con OpenObserve..."
AUTH=$(echo -n "${EMAIL}:${PASSWORD}" | base64)

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Configuración Manual Requerida${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "OpenObserve está corriendo en: ${OPENOBSERVE_URL}"
echo "Credenciales:"
echo "  Email: ${EMAIL}"
echo "  Password: ${PASSWORD}"
echo ""
echo "Por favor, realiza los siguientes pasos manualmente:"
echo ""
echo "1. Abrir OpenObserve: ${OPENOBSERVE_URL}"
echo ""
echo "2. Login con las credenciales de arriba"
echo ""
echo "3. Configurar Data Source (Prometheus):"
echo "   - Ir a 'Settings' → 'Data Sources'"
echo "   - Click en 'Add Data Source'"
echo "   - Seleccionar 'Prometheus'"
echo "   - URL: http://prometheus:9090"
echo "   - Name: Prometheus"
echo "   - Click en 'Save & Test'"
echo ""
echo "4. Agregar métricas de Node Exporter:"
echo "   - Data Source Type: Prometheus"
echo "   - URL: http://node-exporter:9100"
echo "   - Name: Node Exporter"
echo ""
echo "5. Crear Dashboards:"
echo "   Ver archivo: dashboards-openobserve.md para queries"
echo ""
echo "=========================================="
echo ""

# Crear archivo con queries para los dashboards
cat > dashboards-openobserve.md << 'EOF'
# Dashboards para OpenObserve

## Dashboard 1: Pipeline Performance (4 Paneles)

### Panel 1: Build Duration
- **Tipo**: Time Series
- **Query**: 
```
default_jenkins_builds_last_build_duration_milliseconds / 1000
```
- **Unit**: seconds

### Panel 2: Success Rate
- **Tipo**: Gauge
- **Query**:
```
(1 - (sum(default_jenkins_builds_failed_build_count_total) / sum(default_jenkins_builds_duration_milliseconds_summary_count))) * 100
```
- **Unit**: percent
- **Min**: 0, **Max**: 100

### Panel 3: Total Executions
- **Tipo**: Stat/Counter
- **Query**:
```
default_jenkins_builds_duration_milliseconds_summary_count
```
- **Unit**: short

### Panel 4: Queue Size
- **Tipo**: Time Series
- **Query**:
```
default_jenkins_queue_size_value
```
- **Unit**: short

---

## Dashboard 2: Application Performance (4 Paneles)

### Panel 1: CPU Usage
- **Tipo**: Gauge
- **Query**:
```
100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```
- **Unit**: percent
- **Thresholds**: 0-70 (green), 70-90 (yellow), 90-100 (red)

### Panel 2: Memory Usage
- **Tipo**: Gauge
- **Query**:
```
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
```
- **Unit**: percent
- **Thresholds**: 0-70 (green), 70-90 (yellow), 90-100 (red)

### Panel 3: Network Throughput
- **Tipo**: Time Series
- **Query**:
```
rate(node_network_receive_bytes_total[1m]) * 8 / 1000000
```
- **Unit**: Mbps
- **Legend**: {{device}} - Received

### Panel 4: Disk I/O
- **Tipo**: Time Series
- **Query**:
```
rate(node_disk_read_bytes_total[1m]) / 1024 / 1024
```
- **Unit**: MB/s
- **Legend**: {{device}} - Read
EOF

echo -e "${GREEN}Archivo de queries creado: dashboards-openobserve.md${NC}"
echo ""
echo "Listo para configurar dashboards manualmente en OpenObserve!"

