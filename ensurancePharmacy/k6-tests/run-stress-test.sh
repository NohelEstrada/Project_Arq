#!/bin/bash

# Script para ejecutar tests de stress con k6
# Uso: ./run-stress-test.sh [puerto] [vus] [duration]

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "========================================"
echo "  Pharmacy Stress Test - k6"
echo "========================================"
echo ""

# Parámetros con valores por defecto
PORT=${1:-8101}
VUS=${2:-50}
DURATION=${3:-3m}

echo "Configuración del test:"
echo "  Puerto backend: $PORT"
echo "  Usuarios virtuales (VUs): $VUS"
echo "  Duración: $DURATION"
echo ""

# Verificar que k6 está instalado
if ! command -v k6 &> /dev/null; then
    echo -e "${RED}Error: k6 no está instalado${NC}"
    echo "Instalar con: brew install k6"
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

# Timestamp para los resultados
TIMESTAMP=$(date +%Y%m%d-%H%M%S)

echo ""
echo -e "${YELLOW}Ejecutando test de stress con k6...${NC}"
echo "Esto tomará aproximadamente $DURATION"
echo ""

# Ejecutar k6
k6 run pharmacy-stress-test.js \
    --out json=results/test-results-${TIMESTAMP}.json \
    --summary-export=results/summary-${TIMESTAMP}.json \
    -e BASE_URL=http://localhost \
    -e PORT=$PORT \
    -e API_PREFIX=api2

echo ""
echo -e "${GREEN}========================================"
echo "  Test completado exitosamente!"
echo "========================================${NC}"
echo ""
echo "Resultados guardados en:"
echo "  - JSON Results: results/test-results-${TIMESTAMP}.json"
echo "  - Summary: results/summary-${TIMESTAMP}.json"
echo ""

# Generar reporte HTML si es posible
if [ -f "results/test-results-${TIMESTAMP}.json" ]; then
    echo "Generando reporte HTML..."
    
    # Crear HTML simple con los resultados
    cat > results/report-${TIMESTAMP}.html << EOF
<!DOCTYPE html>
<html>
<head>
    <title>k6 Test Report - ${TIMESTAMP}</title>
    <style>
        body { 
            font-family: Arial, sans-serif; 
            margin: 40px; 
            background: #f5f5f5; 
        }
        .container { 
            max-width: 1200px; 
            margin: 0 auto; 
            background: white; 
            padding: 30px; 
            border-radius: 8px; 
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        h1 { 
            color: #333; 
            border-bottom: 3px solid #7d64ff; 
            padding-bottom: 10px;
        }
        h2 { 
            color: #555; 
            margin-top: 30px;
        }
        .metric { 
            background: #f9f9f9; 
            padding: 15px; 
            margin: 10px 0; 
            border-left: 4px solid #7d64ff;
            border-radius: 4px;
        }
        .metric-name { 
            font-weight: bold; 
            color: #7d64ff;
        }
        .metric-value { 
            font-size: 24px; 
            color: #333;
            margin: 5px 0;
        }
        .success { color: #4caf50; }
        .warning { color: #ff9800; }
        .error { color: #f44336; }
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
        }
        th, td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        th {
            background-color: #7d64ff;
            color: white;
        }
        tr:hover {
            background-color: #f5f5f5;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚀 k6 Stress Test Report</h1>
        <p><strong>Timestamp:</strong> ${TIMESTAMP}</p>
        <p><strong>Target:</strong> http://localhost:${PORT}/api2</p>
        
        <h2>📊 Test Configuration</h2>
        <div class="metric">
            <div class="metric-name">Virtual Users (VUs)</div>
            <div class="metric-value">${VUS}</div>
        </div>
        <div class="metric">
            <div class="metric-name">Test Duration</div>
            <div class="metric-value">${DURATION}</div>
        </div>
        
        <h2>📈 Performance Metrics</h2>
        <p>Check the summary JSON file for detailed metrics:</p>
        <code>results/summary-${TIMESTAMP}.json</code>
        
        <h2>📝 Endpoints Tested</h2>
        <table>
            <tr>
                <th>Endpoint</th>
                <th>Method</th>
                <th>Description</th>
            </tr>
            <tr>
                <td>/api2/medicines</td>
                <td>GET</td>
                <td>List all medicines</td>
            </tr>
            <tr>
                <td>/api2/categories</td>
                <td>GET</td>
                <td>List all categories</td>
            </tr>
            <tr>
                <td>/api2/prescriptions</td>
                <td>GET</td>
                <td>List all prescriptions</td>
            </tr>
            <tr>
                <td>/api2/medicines/:id</td>
                <td>GET</td>
                <td>Get medicine details</td>
            </tr>
        </table>
        
        <h2>🎯 Thresholds Configured</h2>
        <ul>
            <li><strong>95th Percentile:</strong> &lt; 1000ms</li>
            <li><strong>99th Percentile:</strong> &lt; 2000ms</li>
            <li><strong>Error Rate:</strong> &lt; 1%</li>
        </ul>
        
        <h2>📊 View Detailed Results</h2>
        <p>For detailed metrics, check:</p>
        <ul>
            <li><code>results/test-results-${TIMESTAMP}.json</code> - Raw data</li>
            <li><code>results/summary-${TIMESTAMP}.json</code> - Summary with all metrics</li>
        </ul>
        
        <h2>💡 Next Steps</h2>
        <ol>
            <li>Review the summary JSON for detailed metrics</li>
            <li>Check Grafana/OpenObserve for system metrics during the test</li>
            <li>Compare results with JMeter tests</li>
        </ol>
    </div>
</body>
</html>
EOF
    
    echo -e "${GREEN}Reporte HTML generado: results/report-${TIMESTAMP}.html${NC}"
    echo ""
    echo "Para ver el reporte:"
    echo "  open results/report-${TIMESTAMP}.html"
fi

echo ""
echo "Para ejecutar de nuevo con diferentes parámetros:"
echo "  ./run-stress-test.sh [puerto] [usuarios] [duración]"
echo "Ejemplos:"
echo "  ./run-stress-test.sh 8101 100 5m    # 100 usuarios, 5 minutos"
echo "  ./run-stress-test.sh 8084 25 2m     # Test ligero en DEV"
echo ""

# Mostrar resumen rápido si hay summary
if [ -f "results/summary-${TIMESTAMP}.json" ]; then
    echo -e "${BLUE}Resumen Rápido:${NC}"
    cat results/summary-${TIMESTAMP}.json | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    metrics = data.get('metrics', {})
    
    if 'http_reqs' in metrics:
        print(f\"  Total Requests: {metrics['http_reqs']['values']['count']}\")
    
    if 'http_req_duration' in metrics:
        avg = metrics['http_req_duration']['values']['avg']
        p95 = metrics['http_req_duration']['values']['p(95)']
        p99 = metrics['http_req_duration']['values']['p(99)']
        print(f\"  Avg Response Time: {avg:.2f}ms\")
        print(f\"  95th Percentile: {p95:.2f}ms\")
        print(f\"  99th Percentile: {p99:.2f}ms\")
    
    if 'http_req_failed' in metrics:
        fail_rate = metrics['http_req_failed']['values']['rate'] * 100
        print(f\"  Error Rate: {fail_rate:.2f}%\")
    
    # Checks
    if 'checks' in metrics:
        pass_rate = metrics['checks']['values']['rate'] * 100
        print(f\"  Checks Passed: {pass_rate:.2f}%\")
except Exception as e:
    print(f\"  (Error parsing summary: {e})\")
" 2>/dev/null || echo "  Ver archivo JSON para detalles completos"
fi

echo ""

