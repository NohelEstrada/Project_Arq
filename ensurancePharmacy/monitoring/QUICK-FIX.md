# 🚀 Quick Fix - Ver Datos Inmediatamente

## ✅ Estado Actual

Prometheus está funcionando y recolectando datos:
- ✅ **prometheus**: UP - Funcionando
- ✅ **node-exporter**: UP - Funcionando (¡Tiene 64+ métricas!)
- ❌ **jenkins**: DOWN - Requiere plugin
- ❌ **pharmacy-backend**: DOWN - No configurado
- ❌ **sonarqube**: DOWN - No configurado

## 📊 Ver Datos AHORA en Grafana

### Paso 1: Verificar en Prometheus (5 segundos)

1. Abrir: http://localhost:9090
2. En el campo de query, escribir: `up`
3. Click en "Execute"
4. **Deberías ver**: 5 targets (2 con valor "1" = UP)

### Paso 2: Ver Métricas de Sistema en Prometheus

En Prometheus, prueba estas queries:

```promql
# Ver CPU
node_cpu_seconds_total

# Ver Memoria
node_memory_MemAvailable_bytes

# Ver todas las métricas disponibles
{job="node-exporter"}
```

### Paso 3: Crear Panel Simple en Grafana

1. **Abrir Grafana**: http://localhost:3000 (admin/admin)

2. **Ir a Application Performance Dashboard**:
   - Dashboards → Application Performance Dashboard

3. **Cambiar el rango de tiempo**:
   - Arriba derecha, hay un selector de tiempo
   - Cambiar de "Last 1 hour" a **"Last 5 minutes"**
   - O click en "Last 5 minutes" y elegir **"Last 30 seconds"** con refresh automático

4. **Ahora deberías ver datos** en las gráficas 1 y 2:
   - ✅ CPU Usage
   - ✅ Memory Usage

### Paso 4: Solucionar Gráficas 3 y 4 (HTTP y Response Time)

Estas gráficas están buscando métricas del backend que no está configurado. Vamos a cambiarlas temporalmente:

#### Opción A: Usar Grafana UI (Más fácil)

1. En el dashboard, click en el título de "HTTP Request Rate"
2. Click en "Edit"
3. Cambiar la query de:
   ```
   rate(http_server_requests_seconds_count{application="pharmacy-backend"}[5m])
   ```
   A:
   ```
   rate(node_network_receive_bytes_total[5m])
   ```
4. Click en "Apply"
5. Hacer lo mismo con "Response Time", cambiar a:
   ```
   rate(node_disk_read_bytes_total[5m])
   ```

#### Opción B: Dashboard Simplificado (Mejor)

Voy a crear un dashboard que use solo métricas disponibles:

## 🎯 Dashboard que Funciona AHORA

### Queries que SÍ funcionan ahora mismo:

```promql
# 1. CPU Usage
100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)

# 2. Memory Usage  
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100

# 3. Network Received (en lugar de HTTP requests)
rate(node_network_receive_bytes_total[1m]) * 8 / 1000000

# 4. Disk I/O (en lugar de Response Time)
rate(node_disk_read_bytes_total[1m]) / 1024 / 1024
```

## 🔧 Solución Inmediata

### Para ver datos AHORA:

1. **Abrir Grafana**: http://localhost:3000

2. **Crear un panel rápido**:
   - Click en "+" en el menú izquierdo
   - "Create Dashboard"
   - "Add visualization"
   - Seleccionar "Prometheus"

3. **Agregar query de CPU**:
   ```promql
   100 - (avg(rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
   ```

4. **Click en "Run query"**

5. **¡Deberías ver datos!** 📊

## ⚙️ Para ver datos del Pipeline (Jenkins)

Jenkins necesita el plugin. Instalarlo:

1. **Abrir Jenkins**: http://localhost:8081
2. **Manage Jenkins** → **Plugin Manager** → **Available**
3. **Buscar**: "Prometheus metrics"
4. **Instalar** y **reiniciar** Jenkins
5. **Verificar**: http://localhost:8081/prometheus (deberías ver métricas)
6. **Esperar 15-30 segundos** para que Prometheus las recolecte
7. **Ejecutar un build** para generar datos
8. **Refrescar Grafana**

## 📈 Por qué dice "No data"

### Razón 1: Rango de Tiempo
- Grafana busca datos en "Last 1 hour"
- Prometheus solo tiene datos de los últimos minutos
- **Solución**: Cambiar a "Last 5 minutes"

### Razón 2: Métricas no Disponibles
- El dashboard busca métricas de Jenkins/Backend
- Esos servicios no están configurados aún
- **Solución**: Ver solo CPU/Memory que SÍ funcionan

### Razón 3: Necesita Tiempo
- Prometheus recolecta cada 15 segundos
- Necesita al menos 2-3 scrapes para mostrar gráficas
- **Solución**: Esperar 1 minuto

## ✅ Verificación Rápida

```bash
# 1. Ver targets
open http://localhost:9090/targets

# 2. Ver query simple
open "http://localhost:9090/graph?g0.expr=node_cpu_seconds_total"

# 3. Ver Grafana
open http://localhost:3000
```

## 🎬 Demo Inmediata

Si necesitas demostrar AHORA:

1. **Abrir Prometheus**: http://localhost:9090
2. **Mostrar query**: `node_memory_MemTotal_bytes / 1024 / 1024 / 1024` (GB de RAM)
3. **Mostrar gráfica**: Click en "Graph"
4. **Explicar**: "Aquí Prometheus recolecta métricas del sistema cada 15 segundos"

## 📞 Si Sigue Sin Funcionar

```bash
# Ver logs de Prometheus
docker logs prometheus-prod

# Ver logs de Grafana
docker logs grafana-prod

# Reiniciar servicios
cd ensurancePharmacy
docker-compose -f docker-compose.prod.yml restart prometheus grafana
```

---

**TL;DR**: 
- ✅ Prometheus funciona
- ✅ Node-exporter funciona
- ✅ Hay datos de CPU y Memoria
- ⚠️ Cambiar rango de tiempo en Grafana a "Last 5 minutes"
- ⚠️ Jenkins necesita plugin para mostrar datos de pipeline

