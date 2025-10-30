#!/bin/bash

echo "📊 Importando Dashboards a OpenObserve"
echo "========================================"
echo ""

# Configuración
OPENOBSERVE_URL="http://localhost:5080"
EMAIL="admin@pharmacy.com"
PASSWORD="Complexpass#123"
ORG="default"

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Obtener token de autenticación
echo "🔐 Autenticando con OpenObserve..."
AUTH=$(echo -n "$EMAIL:$PASSWORD" | base64)

# Verificar que OpenObserve está respondiendo
if ! curl -f -s "${OPENOBSERVE_URL}/api/default/_search" -H "Authorization: Basic ${AUTH}" > /dev/null 2>&1; then
    echo -e "${RED}❌ Error: No se puede conectar a OpenObserve en ${OPENOBSERVE_URL}${NC}"
    echo "   Verifica que OpenObserve esté corriendo: docker ps | grep openobserve"
    exit 1
fi
echo -e "${GREEN}✅ Conexión exitosa${NC}"
echo ""

# Función para importar dashboard
import_dashboard() {
    local FILE=$1
    local NAME=$(basename "$FILE" .json)
    
    echo "📥 Importando: $NAME..."
    
    if [ ! -f "$FILE" ]; then
        echo -e "${RED}❌ Archivo no encontrado: $FILE${NC}"
        return 1
    fi
    
    # Leer el contenido del JSON
    DASHBOARD_JSON=$(cat "$FILE")
    
    # Crear el dashboard via API
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
        "${OPENOBSERVE_URL}/api/${ORG}/dashboards" \
        -H "Authorization: Basic ${AUTH}" \
        -H "Content-Type: application/json" \
        -d "$DASHBOARD_JSON")
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ]; then
        echo -e "${GREEN}✅ Dashboard importado exitosamente${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  Dashboard puede ya existir o hubo un problema${NC}"
        echo "   HTTP Code: $HTTP_CODE"
        echo "   Response: $BODY" | head -5
        return 1
    fi
}

# Directorio de dashboards
DASHBOARD_DIR="$(dirname "$0")"

echo "📂 Buscando dashboards en: $DASHBOARD_DIR"
echo ""

# Importar dashboard de sistema
if [ -f "${DASHBOARD_DIR}/dashboard-system-metrics.json" ]; then
    import_dashboard "${DASHBOARD_DIR}/dashboard-system-metrics.json"
    echo ""
else
    echo -e "${RED}❌ No se encontró: dashboard-system-metrics.json${NC}"
fi

# Importar dashboard de pipeline
if [ -f "${DASHBOARD_DIR}/dashboard-pipeline-metrics.json" ]; then
    import_dashboard "${DASHBOARD_DIR}/dashboard-pipeline-metrics.json"
    echo ""
else
    echo -e "${RED}❌ No se encontró: dashboard-pipeline-metrics.json${NC}"
fi

echo "=========================================="
echo -e "${GREEN}✨ Proceso completado${NC}"
echo ""
echo "Para ver los dashboards:"
echo "  1. Abre: ${OPENOBSERVE_URL}"
echo "  2. Usuario: ${EMAIL}"
echo "  3. Password: ${PASSWORD}"
echo "  4. Ve a: Dashboards"
echo ""
echo "Nota: Si no ves datos, espera 30 segundos y refresca la página"
echo ""

