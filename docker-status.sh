#!/bin/bash

echo "🔍 Estado de EnsurancePharmacy Docker Services"
echo "=============================================="

# Verificar servicios
echo "📋 Estado de contenedores:"
docker-compose ps

echo ""
echo "🔗 Conectividad:"

# Probar backend
echo -n "Backend Pharmacy (8081): "
if curl -f http://localhost:8081 >/dev/null 2>&1; then
    echo "✅ Conectado"
else
    echo "⚠️  No responde (normal si no hay endpoint raíz)"
fi

# Probar frontend
echo -n "Frontend (9008): "
if curl -f http://localhost:9008 >/dev/null 2>&1; then
    echo "✅ Conectado"
else
    echo "❌ No conectado"
fi

echo ""
echo "🌐 URLs de acceso:"
echo "   Frontend: http://localhost:9008"
echo "   Backend Pharmacy API: http://localhost:8081/api"

echo ""
echo "📊 Logs recientes:"
echo "Backend (últimas 5 líneas):"
docker-compose logs backend --tail=5 | sed 's/^/  /'

echo ""
echo "Frontend (últimas 5 líneas):"
docker-compose logs frontend --tail=5 | sed 's/^/  /'

echo ""
echo "💡 Comandos útiles:"
echo "   Ver logs:        docker-compose logs -f"
echo "   Reiniciar:       docker-compose restart [service]"
echo "   Detener todo:    ./docker-stop.sh" 