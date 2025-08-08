# 🐳 EnsurancePharmacy - Configuración Docker

Este documento explica cómo ejecutar la aplicación completa de EnsurancePharmacy usando Docker, incluyendo tanto el backend (Java con SQLite) como el frontend (Vue.js).

## 🏗️ Arquitectura

```
┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │
│   Vue.js        │◄──►│   Java/Spring   │
│   Port: 9008    │    │   Port: 8080    │
│   (ensurance)   │    │   (backv4)      │
└─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   SQLite        │
                       │   Database      │
                       │   (local file)  │
                       └─────────────────┘
```

## 🔧 Prerequisitos

- **Docker** (versión 20.10 o superior)
- **Docker Compose** (versión 1.29 o superior)

### Verificar instalación:
```bash
docker --version
docker-compose --version
```

## 🚀 Inicio Rápido

### 1. Clonar y navegar al proyecto
```bash
cd /Users/nohelestradap/Documents/VsCode/ensurancePharmacy
```

### 2. Iniciar todos los servicios
```bash
./docker-start.sh
```

### 3. Acceder a la aplicación
- **Frontend**: http://localhost:9008
- **Backend Pharmacy API**: http://localhost:8081/api

## 📋 Comandos Disponibles

### Iniciar servicios
```bash
./docker-start.sh
```

### Detener servicios
```bash
./docker-stop.sh
```

### Ver logs en tiempo real
```bash
docker-compose logs -f
```

### Ver logs de un servicio específico
```bash
docker-compose logs -f frontend
docker-compose logs -f backend
```

### Reconstruir imágenes
```bash
docker-compose build --no-cache
```

### Reiniciar un servicio específico
```bash
docker-compose restart backend
docker-compose restart frontend
```

## 📁 Estructura del Proyecto

```
ensurancePharmacy/
├── docker-compose.yml          # Orquestación de servicios
├── docker-start.sh            # Script para iniciar
├── docker-stop.sh             # Script para detener
├── README-Docker.md           # Esta documentación
│
├── backv4/                    # Backend Java
│   ├── Dockerfile            # Imagen del backend
│   ├── .dockerignore         # Archivos excluidos
│   ├── pom.xml              # Dependencias Maven
│   ├── pharmacy_db.sqlite   # Base de datos SQLite
│   └── src/                 # Código fuente Java
│
└── ensurance/                 # Frontend Vue.js
    ├── Dockerfile            # Imagen del frontend
    ├── .dockerignore         # Archivos excluidos
    ├── package.json          # Dependencias npm
    ├── vite.config.ts        # Configuración Vite
    └── src/                  # Código fuente Vue.js
```

## 🔧 Configuración

### Variables de Entorno

#### Backend (`backv4`)
- `DB_TYPE=sqlite` - Tipo de base de datos
- `JAVA_OPTS=-Ddb.type=sqlite` - Opciones JVM

#### Frontend (`ensurance`)
- `VITE_API_URL=http://backend:8080` - URL del backend
- `VITE_IP=backend` - IP del backend en Docker
- `VITE_PORT=9008` - Puerto del frontend

### Puertos
- **Frontend**: `9008:9008`
- **Backend**: `8080:8080`

### Red
- **Nombre**: `ensurance-network`
- **Driver**: `bridge`

## 🔍 Debugging

### Problemas Comunes

#### 1. Error "docker: command not found"
```bash
# Instalar Docker Desktop desde https://docker.com
```

#### 2. Error "permission denied"
```bash
chmod +x docker-start.sh docker-stop.sh
```

#### 3. Puerto en uso
```bash
# Verificar qué está usando el puerto
lsof -i :8080
lsof -i :9008

# Cambiar puerto en docker-compose.yml si es necesario
```

#### 4. Frontend no conecta al backend
```bash
# Verificar que el backend esté corriendo
docker-compose logs backend

# Verificar salud del backend
curl http://localhost:8080/api/healthcheck
```

### Logs Detallados
```bash
# Logs de todos los servicios
docker-compose logs

# Logs de construcción
docker-compose build --progress=plain

# Entrar al contenedor para debugging
docker-compose exec backend bash
docker-compose exec frontend sh
```

## 🔄 Desarrollo

### Hot Reload
- **Frontend**: ✅ Habilitado (cambios en `src/` se reflejan automáticamente)
- **Backend**: ❌ Requiere reinicio del contenedor

### Cambios en el código

#### Frontend (automático)
Los cambios en `ensurance/src/` se reflejan inmediatamente.

#### Backend (manual)
```bash
docker-compose restart backend
```

### Base de Datos
La base de datos SQLite se persiste en:
```
./backv4/pharmacy_db.sqlite
```

## 🛠️ Comandos de Mantenimiento

### Limpiar todo
```bash
# Detener y eliminar contenedores, redes y volúmenes
docker-compose down --volumes --remove-orphans

# Eliminar también las imágenes
docker-compose down --rmi all --volumes --remove-orphans
```

### Actualizar dependencias
```bash
# Backend: Actualizar Maven dependencies
docker-compose exec backend mvn clean install

# Frontend: Actualizar npm dependencies
docker-compose exec frontend npm install
```

### Backup de la base de datos
```bash
cp backv4/pharmacy_db.sqlite backv4/pharmacy_db.backup.$(date +%Y%m%d_%H%M%S).sqlite
```

## 📊 Monitoreo

### Estado de los servicios
```bash
docker-compose ps
```

### Uso de recursos
```bash
docker stats
```

### Salud del sistema
```bash
# Verificar backend
curl http://localhost:8080/api/healthcheck

# Verificar frontend
curl http://localhost:9008
```

## 🔐 Seguridad

### Variables sensibles
Las credenciales están configuradas para desarrollo local. Para producción:
1. Usar archivos `.env` separados
2. Configurar secrets de Docker
3. Implementar SSL/TLS

### Red
Los servicios se comunican a través de una red privada Docker.

## 📝 Notas

1. **Persistencia**: La base de datos SQLite se mantiene entre reinicios
2. **Desarrollo**: El frontend tiene hot reload habilitado
3. **Logs**: Se guardan automáticamente y son accesibles via `docker-compose logs`
4. **Performance**: Para producción, considerar usar imágenes multi-stage builds

## 🆘 Soporte

Si encuentras problemas:

1. Revisa los logs: `docker-compose logs`
2. Verifica el estado: `docker-compose ps`
3. Consulta esta documentación
4. Revisa la configuración en `docker-compose.yml`

---

**¡Listo para desarrollar!** 🎉 