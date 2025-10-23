#!/bin/bash

# Script para probar las alertas del sistema de monitoreo
# Este script ejecuta diferentes pruebas para verificar que las alertas funcionan correctamente

echo "🧪 Sistema de Pruebas de Alertas"
echo "================================="
echo ""

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para mostrar menú
show_menu() {
    echo ""
    echo "Selecciona el tipo de alerta a probar:"
    echo ""
    echo "  1) 🔥 Alerta de CPU Alta (70%+)"
    echo "  2) 🔴 Alerta de CPU Crítica (90%+)"
    echo "  3) 💾 Alerta de Memoria Alta"
    echo "  4) 🔧 Alerta de Pipeline Fallido"
    echo "  5) 📊 Ver estado actual de alertas"
    echo "  6) 📧 Probar notificación de Email"
    echo "  7) 💬 Probar notificación de Slack"
    echo "  8) 🌐 Abrir interfaces de monitoreo"
    echo "  9) ❌ Salir"
    echo ""
    read -p "Opción: " option
    return $option
}

# Función para probar CPU
test_cpu_alert() {
    local intensity=$1
    echo ""
    echo -e "${YELLOW}🔥 Iniciando prueba de alerta de CPU...${NC}"
    echo ""
    
    if [ ! -d "../jmeter-tests" ]; then
        echo -e "${RED}❌ Error: No se encontró el directorio jmeter-tests${NC}"
        return 1
    fi
    
    echo "📊 Generando carga de CPU..."
    echo "   - Usuarios virtuales: $intensity"
    echo "   - Duración: 8 minutos"
    echo "   - Esperando 5 minutos para que se active la alerta..."
    echo ""
    
    cd ../jmeter-tests
    ./run-stress-test.sh $intensity 8 8101 &
    JMETER_PID=$!
    cd - > /dev/null
    
    echo "✅ Stress test iniciado (PID: $JMETER_PID)"
    echo ""
    echo "⏰ Cronograma esperado:"
    echo "   - 0-5 min: Generando carga"
    echo "   - 5 min: Alerta de WARNING (CPU > 70%)"
    echo "   - Recibirás notificación por email y Slack"
    echo ""
    echo "📊 Monitorea en tiempo real:"
    echo "   - Grafana: http://localhost:3000/d/app-perf"
    echo "   - Prometheus: http://localhost:9090/graph?g0.expr=node_cpu_seconds_total"
    echo ""
    
    read -p "Presiona Enter para detener el stress test..."
    kill $JMETER_PID 2>/dev/null
    echo "✅ Stress test detenido"
}

# Función para probar pipeline
test_pipeline_alert() {
    echo ""
    echo -e "${YELLOW}🔧 Prueba de alerta de Pipeline${NC}"
    echo ""
    echo "Para probar la alerta de pipeline fallido:"
    echo ""
    echo "1. Haz un cambio que falle el build en Jenkins"
    echo "2. O simula un fallo con este comando:"
    echo ""
    echo -e "${GREEN}   # En el repositorio del proyecto:${NC}"
    echo "   echo 'syntax error' > test-error.java"
    echo "   git add test-error.java"
    echo "   git commit -m 'Test: Trigger pipeline failure'"
    echo "   git push"
    echo ""
    echo "3. Espera 1 minuto"
    echo "4. Deberías recibir alerta CRÍTICA por email y Slack"
    echo ""
}

# Función para ver estado de alertas
check_alerts_status() {
    echo ""
    echo -e "${YELLOW}📊 Estado Actual de Alertas${NC}"
    echo ""
    
    # Verificar servicios
    echo "🔍 Verificando servicios de monitoreo..."
    echo ""
    
    if docker ps | grep -q "grafana-prod"; then
        echo -e "  ${GREEN}✅ Grafana${NC} - http://localhost:3000"
    else
        echo -e "  ${RED}❌ Grafana no está corriendo${NC}"
    fi
    
    if docker ps | grep -q "prometheus-prod"; then
        echo -e "  ${GREEN}✅ Prometheus${NC} - http://localhost:9090"
    else
        echo -e "  ${RED}❌ Prometheus no está corriendo${NC}"
    fi
    
    if docker ps | grep -q "openobserve-prod"; then
        echo -e "  ${GREEN}✅ OpenObserve${NC} - http://localhost:5080"
    else
        echo -e "  ${RED}❌ OpenObserve no está corriendo${NC}"
    fi
    
    echo ""
    echo "📋 Métricas actuales:"
    echo ""
    
    # CPU
    if command -v curl &> /dev/null; then
        CPU=$(curl -s 'http://localhost:9090/api/v1/query?query=100%20-%20(avg(rate(node_cpu_seconds_total%7Bmode%3D%22idle%22%7D%5B5m%5D))%20*%20100)' | grep -o '"result":\[[^]]*\]' | grep -o '[0-9.]*"' | head -1 | tr -d '"' 2>/dev/null)
        if [ ! -z "$CPU" ]; then
            echo "  🖥️  CPU Usage: ${CPU}%"
        else
            echo "  🖥️  CPU Usage: No disponible"
        fi
        
        # Memoria
        MEM=$(curl -s 'http://localhost:9090/api/v1/query?query=(1%20-%20(node_memory_MemAvailable_bytes%20%2F%20node_memory_MemTotal_bytes))%20*%20100' | grep -o '"result":\[[^]]*\]' | grep -o '[0-9.]*"' | head -1 | tr -d '"' 2>/dev/null)
        if [ ! -z "$MEM" ]; then
            echo "  💾 Memory Usage: ${MEM}%"
        else
            echo "  💾 Memory Usage: No disponible"
        fi
    else
        echo "  ⚠️  curl no instalado, no se pueden obtener métricas"
    fi
    
    echo ""
    echo "🔔 Enlaces útiles:"
    echo "  - Estado de alertas: http://localhost:3000/alerting/list"
    echo "  - Historial: http://localhost:3000/alerting/history"
    echo "  - Contact Points: http://localhost:3000/alerting/notifications"
    echo ""
}

# Función para probar email
test_email() {
    echo ""
    echo -e "${YELLOW}📧 Prueba de Notificación por Email${NC}"
    echo ""
    echo "1. Abre Grafana: http://localhost:3000"
    echo "2. Ve a: Alerting → Contact points"
    echo "3. Busca 'Email Notifications'"
    echo "4. Haz clic en el botón 'Test'"
    echo "5. Verifica tu bandeja de entrada en:"
    echo "   - dnestrada@unis.edu.gt"
    echo "   - jflores@unis.edu.gt"
    echo ""
    read -p "¿Abrir Grafana en el navegador? (y/n): " response
    if [[ "$response" == "y" ]]; then
        if command -v open &> /dev/null; then
            open "http://localhost:3000/alerting/notifications"
        else
            xdg-open "http://localhost:3000/alerting/notifications" 2>/dev/null
        fi
    fi
}

# Función para probar Slack
test_slack() {
    echo ""
    echo -e "${YELLOW}💬 Prueba de Notificación por Slack${NC}"
    echo ""
    echo "Enviando mensaje de prueba a Slack..."
    
    # Cargar webhook URL desde .env
    if [ -f "../.env" ]; then
        source ../.env
    fi
    
    WEBHOOK_URL="${SLACK_WEBHOOK_URL:-https://hooks.slack.com/services/YOUR/WEBHOOK/URL}"
    
    curl -X POST "$WEBHOOK_URL" \
        -H 'Content-Type: application/json' \
        -d '{
            "text": "🧪 *Test de Sistema de Alertas*\n\n*Tipo:* Prueba Manual\n*Sistema:* Pharmacy Monitoring\n*Fecha:* '"$(date '+%Y-%m-%d %H:%M:%S')"'\n\n✅ Si recibes este mensaje, las notificaciones de Slack están funcionando correctamente."
        }'
    
    if [ $? -eq 0 ]; then
        echo ""
        echo -e "${GREEN}✅ Mensaje enviado exitosamente${NC}"
        echo "Verifica el canal #unis-project en Slack"
    else
        echo ""
        echo -e "${RED}❌ Error al enviar mensaje${NC}"
        echo "Verifica la configuración del webhook"
    fi
    echo ""
}

# Función para abrir interfaces
open_interfaces() {
    echo ""
    echo -e "${YELLOW}🌐 Abriendo Interfaces de Monitoreo${NC}"
    echo ""
    
    if command -v open &> /dev/null; then
        # macOS
        open "http://localhost:3000/alerting/list"
        open "http://localhost:5080"
        open "http://localhost:9090/alerts"
        echo "✅ Interfaces abiertas en el navegador"
    elif command -v xdg-open &> /dev/null; then
        # Linux
        xdg-open "http://localhost:3000/alerting/list"
        xdg-open "http://localhost:5080"
        xdg-open "http://localhost:9090/alerts"
        echo "✅ Interfaces abiertas en el navegador"
    else
        echo "URLs de las interfaces:"
        echo "  - Grafana: http://localhost:3000/alerting/list"
        echo "  - OpenObserve: http://localhost:5080"
        echo "  - Prometheus: http://localhost:9090/alerts"
    fi
    echo ""
}

# Main loop
while true; do
    show_menu
    option=$?
    
    case $option in
        1)
            test_cpu_alert 50
            ;;
        2)
            test_cpu_alert 150
            ;;
        3)
            echo ""
            echo "⚠️  Prueba de memoria requiere herramientas especiales"
            echo "Usa el stress test de CPU que también incrementa el uso de memoria"
            ;;
        4)
            test_pipeline_alert
            ;;
        5)
            check_alerts_status
            ;;
        6)
            test_email
            ;;
        7)
            test_slack
            ;;
        8)
            open_interfaces
            ;;
        9)
            echo ""
            echo "👋 ¡Hasta luego!"
            exit 0
            ;;
        *)
            echo -e "${RED}Opción inválida${NC}"
            ;;
    esac
    
    echo ""
    read -p "Presiona Enter para continuar..."
done

