#!/bin/bash

echo "🧪 Probando configuración Docker de ensurancePharmacy..."

# Verificar prerequisitos
echo "🔍 Verificando prerequisitos..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está instalado"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose no está instalado"
    exit 1
fi

echo "✅ Docker y Docker Compose están disponibles"

# Construir y iniciar servicios
echo "🔨 Construyendo servicios..."
docker-compose build

echo "🚀 Iniciando servicios..."
docker-compose up -d

# Esperar a que los servicios estén listos
echo "⏳ Esperando a que los servicios estén listos..."
sleep 60

# Verificar estado de los servicios
echo "📋 Estado de los servicios:"
docker-compose ps

# Probar conectividad
echo "🌐 Probando conectividad..."

# Probar backend
echo "🔧 Probando backend (puerto 8080)..."
if curl -f http://localhost:8080 >/dev/null 2>&1; then
    echo "✅ Backend respondiendo en puerto 8080"
else
    echo "⚠️  Backend no responde o no está listo aún"
fi

# Probar frontend
echo "🎨 Probando frontend (puerto 9008)..."
if curl -f http://localhost:9008 >/dev/null 2>&1; then
    echo "✅ Frontend respondiendo en puerto 9008"
else
    echo "⚠️  Frontend no responde o no está listo aún"
fi

# Mostrar logs recientes
echo "📄 Logs recientes del backend:"
docker-compose logs --tail=10 backend

echo "📄 Logs recientes del frontend:"
docker-compose logs --tail=10 frontend

echo ""
echo "🎉 Test completado!"
echo ""
echo "🌐 URLs para acceder:"
echo "   Frontend: http://localhost:9008"
echo "   Backend:  http://localhost:8080"
echo ""
echo "📊 Para ver logs en tiempo real:"
echo "   docker-compose logs -f"
echo ""
echo "🛑 Para detener:"
echo "   ./docker-stop.sh" 