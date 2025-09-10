# 🚀 Drone CI - Configuración Simple

## ✅ Estado Actual
- ✅ Drone configurado y funcionando
- ✅ ngrok creando túnel público
- ✅ Pipeline migrado desde Jenkins

## 📁 Archivos Importantes

### Configuración Principal:
- **`.drone.yml`** - Pipeline configuration (equivalente al Jenkinsfile)
- **`drone-server-compose.yml`** - Docker Compose para Drone server

### Comando Simple:
- **`drone-simple.sh`** - UN SOLO script para gestionar Drone

## 🎯 Comandos Básicos

```bash
# Iniciar todo
./drone-simple.sh start

# Ver estado
./drone-simple.sh status

# Ver logs si hay problemas
./drone-simple.sh logs

# Detener todo
./drone-simple.sh stop
```

## 🌐 URLs

- **Drone:** https://402ed697efc2.ngrok-free.app (cambia con cada reinicio de ngrok)
- **Panel ngrok:** http://localhost:4040
- **SonarQube:** http://localhost:9000

## 🔄 Pipeline Funcionamiento

### Por Branch:
- **`dev`** → Deploy a Development (puertos 8083/8084)
- **`uat`** → Deploy a UAT (puertos 8090/8091)  
- **`master/main`** → Deploy a Production (puertos 8100/8101)

### Stages:
1. Backend Tests (Maven)
2. Frontend Tests (npm)
3. SonarQube Analysis
4. Quality Gate Check
5. Deploy (según branch)
6. Smoke Tests
7. Notifications

## ⚠️ Importante

- **ngrok URL cambia** cada reinicio
- **Actualiza GitHub OAuth App** cuando cambie la URL
- **Drone funciona igual que Jenkins** - mismos ambientes, mismos puertos

## 🎉 ¡Ya está funcionando!

Tu repositorio debería activarse sin errores ahora.
