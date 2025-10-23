# Prometheus y Grafana - Monitoreo de Pipeline y Aplicación

## 📊 Overview

Este directorio contiene la configuración de Prometheus y Grafana para monitorear:
- **Pipeline Performance**: Métricas de Jenkins CI/CD
- **Application Performance**: Métricas de la aplicación Pharmacy

## 🚀 Inicio Rápido

### 1. Levantar los servicios de monitoreo

```bash
cd ensurancePharmacy
docker-compose -f docker-compose.prod.yml up -d prometheus grafana node-exporter
```

### 2. Acceder a las interfaces

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000
  - Usuario: `admin`
  - Contraseña: `admin`

### 3. Ver los dashboards

En Grafana, navega a:
- **Dashboard → Pipeline Performance Dashboard**: Métricas del pipeline
- **Dashboard → Application Performance Dashboard**: Métricas de la aplicación

## 📈 Dashboards Incluidos

### Pipeline Performance Dashboard (4 gráficas)

1. **Pipeline Build Duration**: Duración de cada build del pipeline
2. **Pipeline Success Rate**: Tasa de éxito de los builds
3. **Total Pipeline Executions**: Número total de ejecuciones
4. **Pipeline Queue Size**: Tamaño de la cola de Jenkins

### Application Performance Dashboard (4 gráficas)

1. **CPU Usage**: Uso de CPU del servidor
2. **Memory Usage**: Uso de memoria del servidor
3. **HTTP Request Rate**: Tasa de peticiones HTTP por segundo
4. **Response Time (95th percentile)**: Tiempo de respuesta en el percentil 95

## ⚙️ Configuración

### Jenkins - Habilitar Prometheus Plugin

1. Instalar el plugin "Prometheus Metrics" en Jenkins:
   ```
   Jenkins → Manage Jenkins → Plugin Manager → Available
   Buscar: "Prometheus metrics"
   Instalar y reiniciar
   ```

2. Configurar el plugin:
   ```
   Jenkins → Manage Jenkins → Configure System
   Prometheus Section:
   - Path: /prometheus
   - Enable collecting path: ✓
   ```

3. Las métricas estarán disponibles en: http://localhost:8081/prometheus

### Backend - Spring Boot Actuator (Métricas)

El backend necesita tener Actuator y Micrometer configurados. Agregar al `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Configurar en `application.properties`:

```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.metrics.export.prometheus.enabled=true
management.endpoint.prometheus.enabled=true
```

## 📊 Métricas Disponibles

### Prometheus Targets

Verificar que todos los targets estén UP en: http://localhost:9090/targets

- ✅ **prometheus**: Prometheus self-monitoring
- ✅ **node-exporter**: System metrics (CPU, Memory, Disk, Network)
- ✅ **jenkins**: Jenkins pipeline metrics
- ✅ **pharmacy-backend**: Application metrics (si está configurado)

### Queries Útiles

#### Pipeline Metrics
```promql
# Duración promedio de builds
avg(jenkins_job_duration{job="jenkins"})

# Tasa de éxito
rate(jenkins_job_success_count[5m]) / rate(jenkins_job_count_total[5m]) * 100

# Builds en cola
jenkins_queue_size_value
```

#### Application Metrics
```promql
# CPU Usage
100 - (avg(rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)

# Memory Usage
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100

# HTTP Request Rate
rate(http_server_requests_seconds_count[5m])

# Response Time P95
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))
```

## 🔧 Troubleshooting

### Prometheus no puede conectar a Jenkins

Si ves el error "connection refused" para el target de Jenkins:

1. Verificar que Jenkins esté corriendo: `curl http://localhost:8081`
2. Verificar que el plugin Prometheus esté instalado
3. Verificar en Prometheus config que usa `host.docker.internal:8081`

### Grafana no muestra datos

1. Verificar que Prometheus esté UP: http://localhost:9090/targets
2. En Grafana, ir a Configuration → Data Sources → Prometheus
3. Hacer clic en "Test" para verificar la conexión
4. Verificar que los queries en los dashboards coincidan con las métricas disponibles

### Backend no expone métricas

1. Verificar que Actuator esté en el pom.xml
2. Verificar que el endpoint esté habilitado: `curl http://localhost:8101/actuator/prometheus`
3. Revisar logs del backend para errores de Actuator

## 📂 Estructura de Archivos

```
monitoring/
├── README.md                           # Este archivo
├── prometheus.yml                      # Config de Prometheus
└── grafana/
    ├── provisioning/
    │   ├── datasources/
    │   │   └── prometheus.yml          # Datasource de Prometheus
    │   └── dashboards/
    │       └── dashboards.yml          # Provisioning de dashboards
    └── dashboards/
        ├── pipeline-performance.json    # Dashboard del pipeline
        └── application-performance.json # Dashboard de la aplicación
```

## 🚨 Sistema de Alertas

### ✅ Alertas Configuradas

El sistema ya tiene **10 alertas automáticas** configuradas que envían notificaciones por **email** y **Slack**:

#### Alertas de Sistema
- 🔴 **CPU Crítica**: > 90% por 2 minutos
- ⚠️ **CPU Alta**: > 70% por 5 minutos
- 🔴 **Memoria Crítica**: > 90% por 2 minutos
- ⚠️ **Memoria Alta**: > 80% por 5 minutos
- ⚠️ **Disco Alto**: > 85% por 5 minutos

#### Alertas de Aplicación
- ⚠️ **Response Time Alto**: > 1000ms por 3 minutos
- ⚠️ **Error Rate Alto**: > 5% por 3 minutos

#### Alertas de Pipeline
- 🔴 **Pipeline Fallido**: Build falla
- ⚠️ **Pipeline Lento**: > 10 minutos
- ⚠️ **Queue Grande**: > 5 trabajos

### 📧 Notificaciones

Las alertas se envían a:
- **Correos**: dnestrada@unis.edu.gt, jflores@unis.edu.gt
- **Slack**: Canal #unis-project

### 🚀 Configuración Rápida

```bash
# 1. Levantar servicios
docker-compose -f docker-compose.prod.yml up -d

# 2. Configurar alertas de OpenObserve
cd monitoring/openobserve
./setup-alerts.sh

# 3. Probar alertas
cd monitoring
./test-alerts.sh
```

### 📚 Documentación de Alertas

- **[ALERTAS-README.md](./ALERTAS-README.md)** - Documentación completa del sistema de alertas
- **[ALERTAS-QUICK-START.md](./ALERTAS-QUICK-START.md)** - Guía rápida de 5 minutos
- **[ALERTAS-CONFIGURACION-SMTP.md](./ALERTAS-CONFIGURACION-SMTP.md)** - Configurar email
- **[ALERTAS-RESUMEN-VISUAL.md](./ALERTAS-RESUMEN-VISUAL.md)** - Resumen visual con ejemplos
- **[test-alerts.sh](./test-alerts.sh)** - Script interactivo para probar alertas

## 🎯 Próximos Pasos

1. ✅ **Alertas configuradas**: 10 alertas automáticas con email y Slack
2. **Agregar más métricas**: 
   - Métricas de negocio (ventas, pedidos, usuarios activos)
   - Métricas de base de datos
   - Métricas de SonarQube (code coverage, bugs, vulnerabilities)
3. **Configurar retención**: Ajustar el tiempo de retención de datos en Prometheus según necesidades

## 📚 Referencias

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Jenkins Prometheus Plugin](https://plugins.jenkins.io/prometheus/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

