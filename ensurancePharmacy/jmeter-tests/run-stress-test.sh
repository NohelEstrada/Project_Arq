#!/bin/bash

# Script para ejecutar tests de stress con Apache JMeter
# Uso: ./run-stress-test.sh [usuarios] [ramp-time] [puerto]

set -e

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "========================================"
echo "  Pharmacy Stress Test - Apache JMeter"
echo "========================================"
echo ""

# Parámetros con valores por defecto
USERS=${1:-50}
RAMP_TIME=${2:-30}
PORT=${3:-8101}
LOOPS=${4:-10}

echo "Configuración del test:"
echo "  Usuarios concurrentes: $USERS"
echo "  Ramp-up time: $RAMP_TIME segundos"
echo "  Puerto backend: $PORT"
echo "  Loops por usuario: $LOOPS"
echo ""

# Verificar que JMeter está instalado
if ! command -v jmeter &> /dev/null; then
    echo -e "${RED}Error: JMeter no está instalado${NC}"
    echo "Instalar con: brew install jmeter"
    exit 1
fi

# Verificar que el backend esté corriendo
echo "Verificando que el backend esté activo..."
if ! curl -s http://localhost:$PORT/api2/medicines > /dev/null; then
    echo -e "${RED}Error: El backend no está corriendo en puerto $PORT${NC}"
    echo "Asegúrate de levantar los servicios primero:"
    echo "  cd ensurancePharmacy"
    echo "  docker-compose -f docker-compose.prod.yml up -d"
    exit 1
fi

echo -e "${GREEN}Backend activo en puerto $PORT${NC}"
echo ""

# Crear directorio de resultados si no existe
mkdir -p results

# Limpiar resultados anteriores
echo "Limpiando resultados anteriores..."
rm -f results/*.jtl results/*.csv results/*.html 2>/dev/null

# Timestamp para los resultados
TIMESTAMP=$(date +%Y%m%d-%H%M%S)

echo ""
echo -e "${YELLOW}Ejecutando test de stress...${NC}"
echo "Esto tomará aproximadamente $(( (USERS * RAMP_TIME) / 60 + 2 )) minutos"
echo ""

# Ejecutar JMeter en modo non-GUI
jmeter -n -t pharmacy-stress-test.jmx \
    -JUSERS=$USERS \
    -JRAMP_TIME=$RAMP_TIME \
    -JPORT=$PORT \
    -l results/test-results-${TIMESTAMP}.jtl \
    -e -o results/html-report-${TIMESTAMP}

echo ""
echo -e "${GREEN}========================================"
echo "  Test completado exitosamente!"
echo "========================================${NC}"
echo ""
echo "Resultados guardados en:"
echo "  - JTL File: results/test-results-${TIMESTAMP}.jtl"
echo "  - HTML Report: results/html-report-${TIMESTAMP}/index.html"
echo ""
echo "Para ver el reporte HTML:"
echo "  open results/html-report-${TIMESTAMP}/index.html"
echo ""

# Mostrar resumen rápido si existe
if [ -f "results/test-results-${TIMESTAMP}.jtl" ]; then
    echo "Resumen rápido:"
    TOTAL_REQUESTS=$(tail -n +2 results/test-results-${TIMESTAMP}.jtl | wc -l | tr -d ' ')
    SUCCESS_REQUESTS=$(tail -n +2 results/test-results-${TIMESTAMP}.jtl | grep ",true," | wc -l | tr -d ' ')
    echo "  Total de requests: $TOTAL_REQUESTS"
    echo "  Requests exitosos: $SUCCESS_REQUESTS"
    if [ $TOTAL_REQUESTS -gt 0 ]; then
        SUCCESS_RATE=$(awk "BEGIN {printf \"%.2f\", ($SUCCESS_REQUESTS / $TOTAL_REQUESTS) * 100}")
        echo "  Success rate: ${SUCCESS_RATE}%"
    fi
fi

echo ""
echo "Para ejecutar de nuevo con diferentes parámetros:"
echo "  ./run-stress-test.sh [usuarios] [ramp-time] [puerto]"
echo "Ejemplo:"
echo "  ./run-stress-test.sh 100 60 8101  # 100 usuarios, 60s ramp, puerto 8101"
echo ""

