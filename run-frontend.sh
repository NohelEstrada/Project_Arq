#!/bin/bash

echo "🏗️  Construyendo imagen del frontend..."
docker build -t pharmacy-frontend ./pharmacy

if [ $? -eq 0 ]; then
    echo "✅ Imagen construida exitosamente"
    echo "🚀 Iniciando contenedor del frontend..."
    docker run -d \
        --name pharmacy-frontend-container \
        -p 8080:8080 \
        -e VUE_APP_API_HOST=localhost \
        -e VUE_APP_IP=localhost \
        pharmacy-frontend
    
    if [ $? -eq 0 ]; then
        echo "✅ Frontend corriendo en http://localhost:8080"
        echo "📊 Para ver logs: docker logs -f pharmacy-frontend-container"
        echo "🛑 Para parar: docker stop pharmacy-frontend-container"
    else
        echo "❌ Error al iniciar el contenedor"
    fi
else
    echo "❌ Error al construir la imagen"
fi 