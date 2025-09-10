# Drone CI Setup Guide

## 1. Instalación de Drone

### Usando Docker Compose:

```yaml
# drone-server-compose.yml
version: '3.7'

services:
  drone-server:
    image: drone/drone:2
    container_name: drone-server
    ports:
      - "3000:80"
    volumes:
      - drone-data:/data
    restart: always
    environment:
      # GitHub Integration
      DRONE_GITHUB_CLIENT_ID: your_github_oauth_app_client_id
      DRONE_GITHUB_CLIENT_SECRET: your_github_oauth_app_client_secret
      DRONE_RPC_SECRET: your_rpc_secret_here
      DRONE_SERVER_HOST: localhost:3000
      DRONE_SERVER_PROTO: http
      
      # Database (SQLite for simplicity)
      DRONE_DATABASE_DRIVER: sqlite3
      DRONE_DATABASE_DATASOURCE: /data/database.sqlite
      
      # User settings
      DRONE_USER_CREATE: username:your_github_username,admin:true

  drone-runner:
    image: drone/drone-runner-docker:1
    container_name: drone-runner
    restart: always
    depends_on:
      - drone-server
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    environment:
      DRONE_RPC_PROTO: http
      DRONE_RPC_HOST: drone-server
      DRONE_RPC_SECRET: your_rpc_secret_here
      DRONE_RUNNER_CAPACITY: 2
      DRONE_RUNNER_NAME: docker-runner

volumes:
  drone-data:
```

## 2. Configuración de Secretos en Drone

Una vez instalado Drone, configura estos secretos:

```bash
# SonarQube Token
drone secret add your-org/your-repo sonar_token your_sonarqube_token

# Email notifications
drone secret add your-org/your-repo email_username your_smtp_username
drone secret add your-org/your-repo email_password your_smtp_password
drone secret add your-org/your-repo email_from your_from_email
```

## 3. GitHub OAuth App

Crea una GitHub OAuth App en: https://github.com/settings/applications/new

- **Application name:** Drone CI
- **Homepage URL:** http://localhost:3000
- **Authorization callback URL:** http://localhost:3000/login

## 4. Comandos de Instalación

### Opción A: Docker Compose
```bash
# Crear el archivo drone-server-compose.yml con el contenido de arriba
docker-compose -f drone-server-compose.yml up -d
```

### Opción B: Instalación binaria
```bash
# macOS
brew install drone-cli

# Linux
curl -L https://github.com/harness/drone-cli/releases/latest/download/drone_linux_amd64.tar.gz | tar zx
sudo install -t /usr/local/bin drone
```

## 5. Verificación

1. Accede a http://localhost:3000
2. Autoriza con GitHub
3. Activa el repositorio
4. Verifica que los secretos estén configurados

## 6. Estructura de Ambientes

- **Development:** Branch `dev` → Puerto 8083/8084
- **UAT:** Branch `uat` → Puerto 8090/8091  
- **Production:** Branch `master/main` → Puerto 8100/8101

## 7. Diferencias Clave con Jenkins

| Aspecto | Jenkins | Drone |
|---------|---------|-------|
| Configuración | Jenkinsfile (Groovy) | .drone.yml (YAML) |
| Agentes | Jenkins nodes | Docker containers |
| Secretos | Jenkins credentials | Drone secrets |
| Triggers | Webhook + pipeline logic | YAML conditions |
| Parallelización | parallel {} blocks | Multiple steps |
| Notificaciones | emailext plugin | drone-email plugin |
