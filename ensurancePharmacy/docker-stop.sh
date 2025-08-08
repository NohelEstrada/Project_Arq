#!/bin/bash

echo "🛑 Deteniendo servicios de ensurancePharmacy..."

docker-compose down

echo "🧹 Limpiando recursos..."
docker-compose down --volumes --remove-orphans

echo "✅ Servicios detenidos correctamente!"

echo ""
echo "🔄 Para reiniciar los servicios:"
echo "   ./docker-start.sh"
echo ""
echo "🗑️  Para limpiar completamente (eliminar imágenes):"
echo "   docker-compose down --rmi all --volumes --remove-orphans" 