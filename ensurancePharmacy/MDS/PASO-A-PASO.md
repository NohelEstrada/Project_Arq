# 🎯 OpenObserve - Paso a Paso (5 Minutos)

## ✅ Configuración Completada

Prometheus ya está configurado para enviar métricas automáticamente a OpenObserve vía `remote_write`.

**No necesitas configurar nada en OpenObserve** - las métricas ya están llegando automáticamente.

---

## 🚀 Paso a Paso - Ver Métricas AHORA

### Paso 1: Abrir OpenObserve

URL: **http://localhost:5080**

Login:
- Email: `admin@pharmacy.com`
- Password: `Complexpass#123`

### Paso 2: Ir a Metrics

1. En el menú lateral izquierdo, click en **"Metrics"**
2. Deberías ver un explorador de métricas

### Paso 3: Explorar Métricas Disponibles

1. En la parte superior, hay un campo de búsqueda
2. Escribe: `node_` o `jenkins_` o `up`
3. Deberías ver una lista de métricas disponibles

### Paso 4: Crear Visualización Rápida

1. En el campo de query, escribe:
```
node_cpu_seconds_total
```

2. Click en **"Run Query"** o presiona Enter

3. Deberías ver una gráfica con datos

### Paso 5: Crear Dashboard de Pipeline (10 minutos)

1. Click en **"Dashboards"** en el menú lateral

2. Click en **"Create New Dashboard"** (botón azul arriba derecha)

3. Configurar:
   - **Name**: `Pipeline Performance`
   - **Description**: `CI/CD Pipeline Metrics`
   - Click **"Save"**

4. **Agregar Panel 1**:
   - Click en **"Add Panel"** (o "+" en la esquina)
   - En **Query**, escribir:
   ```
   default_jenkins_builds_last_build_duration_milliseconds / 1000
   ```
   - **Title**: `Build Duration`
   - **Visualization Type**: Time Series o Line
   - **Unit**: `seconds`
   - Click **"Apply"** o **"Save"**

5. **Agregar Panel 2**:
   - Click en **"Add Panel"**
   - Query:
   ```
   default_jenkins_queue_size_value
   ```
   - **Title**: `Queue Size`
   - **Visualization**: Time Series
   - Save

6. **Agregar Panel 3**:
   - Query:
   ```
   default_jenkins_builds_duration_milliseconds_summary_count
   ```
   - **Title**: `Total Builds`
   - **Visualization**: Stat (número grande)
   - Save

7. **Agregar Panel 4**:
   - Query:
   ```
   (1 - (sum(default_jenkins_builds_failed_build_count_total) / sum(default_jenkins_builds_duration_milliseconds_summary_count))) * 100
   ```
   - **Title**: `Success Rate`
   - **Visualization**: Gauge
   - **Min**: 0, **Max**: 100
   - **Unit**: percent
   - Save

8. **Guardar Dashboard**: Click **"Save Dashboard"** arriba

### Paso 6: Crear Dashboard de Aplicación (10 minutos)

1. **Dashboards** → **Create New Dashboard**
   - Name: `Application Performance`
   - Description: `System Metrics`

2. **Agregar 4 Paneles**:

#### Panel 1: CPU Usage
- Query:
```
100 - (avg(rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```
- Visualization: Gauge
- Unit: percent

#### Panel 2: Memory Usage
- Query:
```
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
```
- Visualization: Gauge
- Unit: percent

#### Panel 3: Network
- Query:
```
rate(node_network_receive_bytes_total[1m]) * 8 / 1000000
```
- Visualization: Time Series
- Unit: Mbps

#### Panel 4: Disk I/O
- Query:
```
rate(node_disk_read_bytes_total[1m]) / 1024 / 1024
```
- Visualization: Time Series
- Unit: MB/s

3. **Guardar Dashboard**

---

## ✅ ¡Listo!

Ahora tienes:

**OpenObserve** (http://localhost:5080):
- ✅ 2 Dashboards
- ✅ 8 Gráficas totales (4 pipeline + 4 aplicación)
- ✅ Métricas llegando automáticamente desde Prometheus

**Grafana** (http://localhost:3000):
- ✅ 2 Dashboards (auto-configurados)
- ✅ 8 Gráficas totales (4 pipeline + 4 aplicación)

---

## 🎬 Para la Demo

### Mostrar Ambas Plataformas:

1. **Grafana** (más tradicional):
   - "Esta es la solución estándar de la industria"
   - Mostrar dashboards pre-configurados
   - "Se auto-configura con provisioning"

2. **OpenObserve** (más moderno):
   - "Esta es una alternativa moderna todo-en-uno"
   - Mostrar los mismos datos
   - "Incluye logs, trazas y métricas en un solo lugar"
   - "Usa 140x menos almacenamiento que Elasticsearch"

### Ejecutar Stress Test

Mientras muestras los dashboards:

```bash
cd ensurancePharmacy/jmeter-tests
./run-stress-test.sh 50 30 8101
```

Verás en tiempo real:
- CPU y Memory subiendo
- Network throughput aumentando
- Las gráficas actualizándose cada 5 segundos

---

## 🔍 Explorar Características de OpenObserve

### Ver Lista de Métricas

1. **Metrics** → **Metric Explorer**
2. Browse todas las métricas disponibles
3. Click en una para ver detalles y valores históricos

### Búsqueda con SQL (Único de OpenObserve)

1. **Metrics** → Click en el botón **"SQL"**
2. Escribir query SQL:
```sql
SELECT * FROM metrics 
WHERE __name__ = 'node_cpu_seconds_total' 
AND time > now() - interval '10 minutes'
LIMIT 100
```
3. Run query
4. Ver resultados en tabla

### Ver Logs (Bonus)

1. **Logs** en menú lateral
2. Aquí podrías ver logs de los contenedores
3. Búsqueda full-text
4. Correlación con métricas

---

## 📊 Comparación Visual

Puedes abrir ambos lado a lado para comparar:

**Ventana 1**: Grafana (http://localhost:3000)  
**Ventana 2**: OpenObserve (http://localhost:5080)

Mostrar que:
- ✅ Ambos muestran las mismas métricas
- ✅ Prometheus alimenta a ambos
- ✅ Grafana: Dashboards automáticos
- ✅ OpenObserve: Más funcionalidades (logs, SQL, búsqueda)

---

## ⚙️ Verificación Rápida

### ¿Las métricas están llegando?

1. Ir a: **Metrics** en OpenObserve
2. En el query box, escribir: `up`
3. Run query
4. Deberías ver métricas con valores 0 o 1 (estado de services)

### Si no ves métricas:

```bash
# 1. Verificar que Prometheus esté enviando
docker logs prometheus-prod | grep -i "remote_write"

# 2. Reiniciar ambos servicios
cd ensurancePharmacy
docker-compose -f docker-compose.prod.yml restart prometheus openobserve

# 3. Esperar 30 segundos
sleep 30

# 4. Verificar de nuevo en la UI
```

---

## 🎯 Queries de Prueba

Para verificar que funciona, prueba estas queries en OpenObserve:

### Query 1: Estado de Servicios
```promql
up
```
Debería mostrar: 1 (UP) o 0 (DOWN) para cada servicio

### Query 2: CPU
```promql
node_cpu_seconds_total
```
Debería mostrar múltiples series (una por core de CPU)

### Query 3: Memoria Total
```promql
node_memory_MemTotal_bytes / 1024 / 1024 / 1024
```
Debería mostrar el total de RAM en GB

### Query 4: Jenkins Builds
```promql
default_jenkins_builds_duration_milliseconds_summary_count
```
Debería mostrar el número de builds (si has ejecutado alguno)

---

**Tiempo total de configuración**: 20-25 minutos  
**Resultado**: 2 dashboards con 8 gráficas funcionando  
**Ventaja**: Métricas automáticas sin configuración manual de scraping

