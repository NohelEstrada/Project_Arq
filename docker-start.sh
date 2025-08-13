#!/bin/bash

echo "🚀 Iniciando servicios de ensurancePharmacy con Docker..."

# Verificar que Docker esté corriendo
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker no está corriendo. Por favor, inicia Docker primero."
    exit 1
fi

# Verificar que docker-compose esté disponible
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Error: docker-compose no está instalado."
    exit 1
fi

echo "🔨 Construyendo imágenes..."
docker-compose build

echo "🚀 Iniciando servicios..."
docker-compose up -d

echo "📋 Estado de los servicios:"
docker-compose ps

echo ""
echo "✅ Servicios iniciados correctamente!"
echo ""
echo "🌐 URLs disponibles:"
echo "   Frontend: http://localhost:9008"
echo "   Backend Pharmacy API: http://localhost:8081/api"
echo ""
echo "📊 Para ver los logs:"
echo "   docker-compose logs -f"
echo ""
echo "🛑 Para detener los servicios:"
echo "   docker-compose down" 