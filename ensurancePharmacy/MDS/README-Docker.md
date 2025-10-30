# 🐳 Docker Setup - Pharmacy Application

Esta aplicación está compuesta por:
- **Backend**: Java API (backv5) con SQLite - Puerto 8081
- **Frontend**: Vue.js (pharmacy) - Puerto 8080

## 📋 Requisitos Previos

1. Docker instalado y ejecutándose
2. Docker Compose instalado

## 🚀 Formas de Ejecutar

### Opción 1: Usar Docker Compose (Recomendado)

```bash
# Ejecutar toda la aplicación
./run-all.sh

# O manualmente:
docker-compose up --build -d
```

### Opción 2: Ejecutar Servicios Individualmente

```bash
# Solo el backend
./run-backend.sh

# Solo el frontend
./run-frontend.sh
```

### Opción 3: Comandos Docker Manuales

#### Backend
```bash
cd backv5
docker build -t pharmacy-backend .
docker run -d \
  --name pharmacy-backend-container \
  -p 8081:8081 \
  -v $(pwd)/pharmacy_db.sqlite:/app/pharmacy_db.sqlite \
  pharmacy-backend
```

#### Frontend
```bash
cd pharmacy
docker build -t pharmacy-frontend .
docker run -d \
  --name pharmacy-frontend-container \
  -p 8080:8080 \
  -e VUE_APP_API_HOST=localhost \
  -e VUE_APP_IP=localhost \
  pharmacy-frontend
```

## 🌐 URLs de Acceso

- **Frontend**: http://localhost:8080
- **Backend API**: http://localhost:8081

## 📊 Comandos Útiles

```bash
# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs del backend
docker-compose logs -f backend

# Ver logs del frontend
docker-compose logs -f frontend

# Parar todos los servicios
docker-compose down

# Parar y eliminar volúmenes
docker-compose down -v

# Reconstruir imágenes
docker-compose up --build

# Ver estado de contenedores
docker-compose ps
```

## 🗃️ Base de Datos

- La base de datos SQLite (`pharmacy_db.sqlite`) se monta como volumen
- Los datos persisten entre reinicios del contenedor
- Ubicación: `./backv5/pharmacy_db.sqlite`

## 🔧 Configuración

### Backend (backv5)
- Puerto: 8081
- Base de datos: SQLite (pharmacy_db.sqlite)
- Variables de entorno en `.env`

### Frontend (pharmacy)
- Puerto: 8080
- Se conecta automáticamente al backend
- Variables de entorno configuradas para Docker

## 🛠️ Desarrollo

Para desarrollo con recarga automática:

```bash
# Backend (requiere Java 21 y Maven localmente)
cd backv5
mvn exec:java -Dexec.mainClass=com.sources.app.App

# Frontend (requiere Node.js localmente)
cd pharmacy
npm install
npm run serve
```

## ❗ Solución de Problemas

1. **Error "Docker daemon not running"**:
   - Iniciar Docker Desktop o el servicio de Docker

2. **Puerto ocupado**:
   ```bash
   # Ver qué proceso usa el puerto
   lsof -i :8080
   lsof -i :8081
   
   # Parar contenedores existentes
   docker-compose down
   ```

3. **Problemas de permisos con SQLite**:
   ```bash
   # Verificar permisos del archivo
   ls -la backv5/pharmacy_db.sqlite
   
   # Si es necesario, ajustar permisos
   chmod 666 backv5/pharmacy_db.sqlite
   ```

4. **Limpiar todo y empezar de nuevo**:
   ```bash
   docker-compose down -v
   docker system prune -f
   ./run-all.sh
   ``` 

## Pipeline Testing
Esta línea fue agregada para probar el flujo completo de CI/CD - Test $(date) 