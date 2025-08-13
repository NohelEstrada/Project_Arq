#!/bin/bash

echo "🏗️  Construyendo y ejecutando toda la aplicación..."
echo "📊 Esto puede tomar algunos minutos la primera vez..."

# Verificar si docker-compose está disponible
if command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
elif command -v docker &> /dev/null && docker compose version &> /dev/null; then
    COMPOSE_CMD="docker compose"
else
    echo "❌ Docker Compose no está disponible"
    exit 1
fi

echo "🛠️  Usando: $COMPOSE_CMD"

# Parar y limpiar contenedores existentes
echo "🧹 Limpiando contenedores existentes..."
$COMPOSE_CMD down

# Construir y ejecutar
echo "🚀 Construyendo y ejecutando servicios..."
$COMPOSE_CMD up --build -d

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Aplicación iniciada exitosamente!"
    echo ""
    echo "🌐 Frontend (Vue.js): http://localhost:8080"
    echo "🔧 Backend (Java API): http://localhost:8081"
    echo ""
    echo "📊 Para ver logs:"
    echo "   - Backend: $COMPOSE_CMD logs -f backend"
    echo "   - Frontend: $COMPOSE_CMD logs -f frontend"
    echo "   - Ambos: $COMPOSE_CMD logs -f"
    echo ""
    echo "🛑 Para parar todo: $COMPOSE_CMD down"
    echo ""
else
    echo "❌ Error al iniciar la aplicación"
    $COMPOSE_CMD logs
fi 