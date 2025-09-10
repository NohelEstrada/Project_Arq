#!/bin/bash

# Comandos útiles para gestionar Drone CI

echo "🛠️  Drone CI Management Commands"
echo "================================"

case "$1" in
    "start")
        echo "🚀 Iniciando Drone CI..."
        docker-compose -f drone-server-compose.yml up -d
        echo "✅ Drone iniciado en http://localhost:3000"
        ;;
    
    "stop")
        echo "🛑 Deteniendo Drone CI..."
        docker-compose -f drone-server-compose.yml down
        echo "✅ Drone detenido"
        ;;
    
    "status")
        echo "📊 Estado de Drone CI:"
        docker-compose -f drone-server-compose.yml ps
        ;;
    
    "logs")
        echo "📋 Logs de Drone Server:"
        docker-compose -f drone-server-compose.yml logs -f drone-server
        ;;
    
    "logs-runner")
        echo "📋 Logs de Drone Runner:"
        docker-compose -f drone-server-compose.yml logs -f drone-runner
        ;;
    
    "restart")
        echo "🔄 Reiniciando Drone CI..."
        docker-compose -f drone-server-compose.yml restart
        echo "✅ Drone reiniciado"
        ;;
    
    "clean")
        echo "🧹 Limpiando Drone CI (¡CUIDADO: Esto borrará todos los datos!)..."
        read -p "¿Estás seguro? (y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            docker-compose -f drone-server-compose.yml down -v
            docker volume rm ensurancepharmacy_drone-data 2>/dev/null || true
            echo "✅ Drone limpiado completamente"
        else
            echo "❌ Operación cancelada"
        fi
        ;;
    
    "install-cli")
        echo "📥 Instalando Drone CLI..."
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS
            if command -v brew &> /dev/null; then
                brew install drone-cli
            else
                echo "❌ Homebrew no encontrado. Instala manualmente desde:"
                echo "https://github.com/harness/drone-cli/releases"
            fi
        else
            # Linux
            curl -L https://github.com/harness/drone-cli/releases/latest/download/drone_linux_amd64.tar.gz | tar zx
            sudo install -t /usr/local/bin drone
        fi
        echo "✅ Drone CLI instalado"
        ;;
    
    "setup-secrets")
        echo "🔐 Configurando secretos de Drone..."
        echo "Asegúrate de tener DRONE_SERVER y DRONE_TOKEN configurados"
        echo ""
        echo "Ejemplo de configuración:"
        echo "export DRONE_SERVER=http://localhost:3000"
        echo "export DRONE_TOKEN=tu_token_de_usuario_drone"
        echo ""
        echo "Luego ejecuta:"
        echo "drone secret add tu-org/tu-repo sonar_token tu_sonarqube_token"
        echo "drone secret add tu-org/tu-repo email_username tu_smtp_user"
        echo "drone secret add tu-org/tu-repo email_password tu_smtp_pass"
        echo "drone secret add tu-org/tu-repo email_from tu_email"
        ;;
    
    *)
        echo "Uso: $0 {start|stop|status|logs|logs-runner|restart|clean|install-cli|setup-secrets}"
        echo ""
        echo "Comandos disponibles:"
        echo "  start         - Inicia Drone CI"
        echo "  stop          - Detiene Drone CI"
        echo "  status        - Muestra estado de contenedores"
        echo "  logs          - Muestra logs del servidor"
        echo "  logs-runner   - Muestra logs del runner"
        echo "  restart       - Reinicia Drone CI"
        echo "  clean         - Limpia completamente Drone (⚠️  borra datos)"
        echo "  install-cli   - Instala Drone CLI"
        echo "  setup-secrets - Guía para configurar secretos"
        echo ""
        echo "Ejemplos:"
        echo "  ./drone-commands.sh start"
        echo "  ./drone-commands.sh status"
        echo "  ./drone-commands.sh logs"
        ;;
esac
