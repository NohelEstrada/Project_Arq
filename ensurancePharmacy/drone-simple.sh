#!/bin/bash

echo "🚀 Drone CI - Comandos Simples"
echo "=============================="

case "$1" in
    "start")
        echo "▶️  Iniciando Drone..."
        docker-compose -f drone-server-compose.yml up -d
        echo "✅ Drone iniciado en http://localhost:3000"
        ;;
    
    "stop")
        echo "⏹️  Deteniendo Drone..."
        docker-compose -f drone-server-compose.yml down
        pkill -f "ngrok http" 2>/dev/null || true
        echo "✅ Drone y ngrok detenidos"
        ;;
    
    "status")
        echo "📊 Estado:"
        docker-compose -f drone-server-compose.yml ps
        echo ""
        if pgrep -f "ngrok http" > /dev/null; then
            echo "🌐 ngrok: ✅ Ejecutándose"
            if [ -f .ngrok-url ]; then
                echo "📍 URL: $(cat .ngrok-url)"
            fi
        else
            echo "🌐 ngrok: ❌ Detenido"
        fi
        ;;
    
    "logs")
        echo "📋 Logs de Drone:"
        docker-compose -f drone-server-compose.yml logs --tail=20 drone-server
        ;;
    
    "restart")
        echo "🔄 Reiniciando Drone..."
        docker-compose -f drone-server-compose.yml restart
        echo "✅ Drone reiniciado"
        ;;
    
    *)
        echo ""
        echo "Uso: $0 {start|stop|status|logs|restart}"
        echo ""
        echo "📋 Comandos:"
        echo "  start   - Inicia Drone"
        echo "  stop    - Detiene Drone y ngrok"
        echo "  status  - Muestra estado actual"
        echo "  logs    - Muestra logs recientes"
        echo "  restart - Reinicia Drone"
        echo ""
        echo "🌐 URLs importantes:"
        echo "  - Drone local: http://localhost:3000"
        echo "  - ngrok panel: http://localhost:4040"
        echo ""
        ;;
esac
