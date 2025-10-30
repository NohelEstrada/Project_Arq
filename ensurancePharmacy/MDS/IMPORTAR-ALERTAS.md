# 🚨 Importar Dashboard de Alertas a OpenObserve

Este dashboard muestra el estado de las alertas del sistema de monitoreo.

## 📋 Contenido del Dashboard

### Métricas de Alertas:
- 🔴 **Alertas Críticas Activas**: Contador de alertas críticas disparadas
- 🟡 **Alertas Warning Activas**: Contador de alertas de advertencia

### Métricas del Sistema:
- **CPU Usage**: Uso de CPU con línea de tiempo
- **Memoria Usage**: Uso de memoria del sistema
- **Disco Usage**: Uso de disco (/tmp)

### Métricas de Jenkins/Pipeline:
- **Builds Fallidos**: Total de builds que han fallado
- **Builds Exitosos**: Total de builds exitosos
- **Duración del Build**: Tiempo que tarda cada build
- **Tasa de Error**: Porcentaje de builds fallidos
- **Tests Fallando**: Número de tests que están fallando
- **Tests Totales**: Total de tests ejecutados
- **Health Score**: Puntuación de salud del sistema

## 🚀 Cómo Importar

### Opción 1: Importación Manual (Recomendada)

1. **Accede a OpenObserve**:
   ```
   http://localhost:5080
   ```

2. **Ve a Dashboards**:
   - Click en el menú lateral izquierdo
   - Selecciona "Dashboards"

3. **Importar Dashboard**:
   - Click en el botón "Import"
   - Selecciona el archivo: `alertas-dashboard.json`
   - Click en "Import"

4. **Verificar**:
   - El dashboard aparecerá en la lista
   - Click en el nombre para visualizarlo

### Opción 2: Usando cURL

```bash
# Ir al directorio de OpenObserve
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy/monitoring/openobserve

# Importar el dashboard
curl -X POST "http://localhost:5080/api/default/dashboards" \
  -H "Content-Type: application/json" \
  -u "root@example.com:Complexpass#123" \
  -d @alertas-dashboard.json
```

## 📊 Paneles Incluidos

### 1. Alertas Activas (Fila Superior)
- **Panel Izquierdo**: Alertas críticas (severity=critical)
- **Panel Derecho**: Alertas de advertencia (severity=warning)

### 2. Recursos del Sistema (Filas Medias)
- **CPU**: Monitoreo del uso de CPU
- **Memoria**: Monitoreo del uso de memoria
- **Disco**: Monitoreo del uso de disco

### 3. Métricas de Pipeline (Fila Inferior)
- **Builds**: Estado de los builds de Jenkins
- **Tests**: Estadísticas de tests
- **Performance**: Duración y health score

## 🔍 Queries Prometheus Usadas

### Alertas Críticas:
```promql
count(ALERTS{severity="critical",alertstate="firing"})
```

### Alertas Warning:
```promql
count(ALERTS{severity="warning",alertstate="firing"})
```

### CPU Usage:
```promql
100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```

### Memoria Usage:
```promql
(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
```

### Disco Usage:
```promql
(1 - (node_filesystem_avail_bytes{mountpoint="/tmp"} / node_filesystem_size_bytes{mountpoint="/tmp"})) * 100
```

### Error Rate:
```promql
(default_jenkins_builds_failed_build_count_total / default_jenkins_builds_total_build_count_total) * 100
```

### Build Duration:
```promql
default_jenkins_builds_last_build_duration_milliseconds / 60000
```

## ⚠️ Notas Importantes

1. **Prometheus como Fuente de Datos**:
   - Asegúrate de que Prometheus esté configurado en OpenObserve
   - URL: `http://prometheus-prod:9090`

2. **Umbrales de Alertas**:
   - CPU Crítico: > 90%
   - CPU Warning: 70-90%
   - Memoria Crítica: > 90%
   - Memoria Warning: > 80%
   - Disco Warning: > 85%
   - Error Rate: > 5%
   - Build Duration: > 10 minutos

3. **Actualización de Datos**:
   - Los paneles se actualizan automáticamente
   - Frecuencia configurable en OpenObserve

## 🔧 Personalización

Puedes modificar el archivo `alertas-dashboard.json` para:

- Cambiar los umbrales de las alertas
- Agregar nuevos paneles
- Modificar las queries de Prometheus
- Cambiar el layout de los paneles

## 📝 Estructura del JSON

```json
{
  "version": 1,
  "dashboards": [
    {
      "title": "Sistema de Alertas - Pharmacy Monitoring",
      "panels": [
        {
          "id": "panel_id",
          "type": "metric|line",
          "title": "Título del Panel",
          "queries": [...]
        }
      ]
    }
  ]
}
```

## ✅ Verificación

Después de importar, verifica que:

1. ✓ El dashboard aparece en la lista
2. ✓ Todos los paneles cargan datos
3. ✓ Las métricas de alertas muestran valores
4. ✓ Los gráficos de tiempo muestran datos históricos

## 🆘 Solución de Problemas

### Error: "Dashboard import failed"
- Verifica que el JSON es válido
- Revisa los logs de OpenObserve
- Intenta con la importación manual

### Error: "No data in panels"
- Verifica que Prometheus esté corriendo
- Revisa la configuración del datasource
- Verifica que las métricas existan en Prometheus

### Error: "Authentication failed"
- Verifica las credenciales en el comando cURL
- Usa las credenciales correctas de OpenObserve

## 📚 Referencias

- [OpenObserve Dashboards Docs](https://openobserve.ai/docs/dashboards/)
- [Prometheus Query Language](https://prometheus.io/docs/prometheus/latest/querying/basics/)
- Archivo de dashboards: `alertas-dashboard.json`

