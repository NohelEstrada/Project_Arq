#!/bin/bash

echo "🏗️  Construyendo imagen del backend..."
docker build -t pharmacy-backend ./backv5

if [ $? -eq 0 ]; then
    echo "✅ Imagen construida exitosamente"
    echo "🚀 Iniciando contenedor del backend..."
    docker run -d \
        --name pharmacy-backend-container \
        -p 8081:8081 \
        -v $(pwd)/backv5/pharmacy_db.sqlite:/app/pharmacy_db.sqlite \
        pharmacy-backend
    
    if [ $? -eq 0 ]; then
        echo "✅ Backend corriendo en http://localhost:8081"
        echo "📊 Para ver logs: docker logs -f pharmacy-backend-container"
        echo "🛑 Para parar: docker stop pharmacy-backend-container"
    else
        echo "❌ Error al iniciar el contenedor"
    fi
else
    echo "❌ Error al construir la imagen"
fi 