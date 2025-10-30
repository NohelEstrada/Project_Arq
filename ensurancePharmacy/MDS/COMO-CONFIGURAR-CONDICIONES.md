# 🔧 Cómo Configurar Condiciones en OpenObserve

## 📋 Estructura de Condiciones

Para alertas de tipo "custom" en tiempo real, necesitas configurar correctamente el objeto `query_condition`.

### Componentes Principales:

1. **`conditions`**: Filtros sobre las etiquetas/labels de la métrica
2. **`aggregation`**: Cómo agregar los datos y cuándo disparar

## 🎯 Ejemplos por Tipo de Alerta

### 1. CPU Critical (usando idle mode)

```json
{
  "query_condition": {
    "type": "custom",
    "conditions": {
      "items": [
        {
          "column": "mode",
          "operator": "=",
          "value": "idle",
          "ignore_case": false
        }
      ]
    },
    "aggregation": {
      "group_by": ["instance"],
      "function": "avg",
      "having": {
        "column": "value",
        "operator": "<=",
        "value": 10
      }
    }
  }
}
```

**Lógica**: 
- Filtra por `mode=idle` en `node_cpu_seconds_total`
- Agrupa por `instance`
- Usa función `avg` (promedio)
- Se dispara cuando el valor es <= 10% (significa CPU usage > 90%)

### 2. Memory Critical (disponible < 1GB)

```json
{
  "query_condition": {
    "type": "custom",
    "conditions": {
      "items": []
    },
    "aggregation": {
      "group_by": ["instance"],
      "function": "avg",
      "having": {
        "column": "value",
        "operator": "<=",
        "value": 1073741824
      }
    }
  }
}
```

**Lógica**:
- Sin filtros (toma todas las instancias)
- Agrupa por `instance`
- Se dispara cuando memoria disponible <= 1GB (1073741824 bytes)

### 3. Disk Usage (disponible < 15%)

```json
{
  "query_condition": {
    "type": "custom",
    "conditions": {
      "items": [
        {
          "column": "mountpoint",
          "operator": "=",
          "value": "/tmp",
          "ignore_case": false
        }
      ]
    },
    "aggregation": {
      "group_by": ["instance", "mountpoint"],
      "function": "avg",
      "having": {
        "column": "value",
        "operator": "<=",
        "value": 162165019238
      }
    }
  }
}
```

**Lógica**:
- Filtra por `mountpoint=/tmp`
- Agrupa por `instance` y `mountpoint`
- Se dispara cuando espacio disponible <= 15% del total

### 4. Jenkins Build Failed

```json
{
  "query_condition": {
    "type": "custom",
    "conditions": {
      "items": []
    },
    "aggregation": {
      "group_by": ["jenkins_job"],
      "function": "max",
      "having": {
        "column": "value",
        "operator": ">",
        "value": 0
      }
    }
  }
}
```

**Lógica**:
- Sin filtros
- Agrupa por `jenkins_job`
- Usa `max` para tomar el último valor
- Se dispara cuando `result_ordinal > 0` (0 = success, >0 = failed)

## 📝 Operadores Disponibles

### Para `conditions.items[]`:
- `=` - Igual a
- `!=` - Diferente de
- `>` - Mayor que
- `>=` - Mayor o igual que
- `<` - Menor que
- `<=` - Menor o igual que
- `contains` - Contiene
- `not_contains` - No contiene

### Para `aggregation.having`:
- `=` - Igual a
- `!=` - Diferente de
- `>` - Mayor que
- `>=` - Mayor o igual que
- `<` - Menor que
- `<=` - Menor o igual que

## 🔢 Funciones de Agregación

- `count` - Contar registros
- `sum` - Suma de valores
- `avg` - Promedio de valores
- `min` - Valor mínimo
- `max` - Valor máximo
- `median` - Mediana
- `p50`, `p75`, `p90`, `p95`, `p99` - Percentiles

## 💡 Tips para Configurar

### CPU:
- **Stream**: `node_cpu_seconds_total`
- **Condition**: `mode = idle`
- **Aggregation**: `avg`, `having: value <= 10` (para alertar cuando CPU > 90%)

### Memoria:
- **Stream**: `node_memory_MemAvailable_bytes`
- **Condition**: Sin filtros (o filtrar por instance)
- **Aggregation**: `avg`, `having: value <= [bytes]` (ejemplo: 1GB = 1073741824)

### Disco:
- **Stream**: `node_filesystem_avail_bytes`
- **Condition**: `mountpoint = /tmp` (o el mountpoint que quieras)
- **Aggregation**: `avg`, `having: value <= [bytes]`

### Jenkins Build Failed:
- **Stream**: `default_jenkins_builds_last_build_result_ordinal`
- **Condition**: Sin filtros (o filtrar por jenkins_job)
- **Aggregation**: `max`, `having: value > 0`

## 🎨 Estructura Completa de Ejemplo

```json
{
  "name": "Nombre_Alerta",
  "stream_type": "metrics",
  "stream_name": "nombre_metrica",
  "is_real_time": "true",
  "query_condition": {
    "type": "custom",
    "conditions": {
      "groupId": "unique-id",
      "label": "and",
      "items": [
        {
          "column": "nombre_label",
          "operator": "=",
          "value": "valor",
          "ignore_case": false,
          "id": "condition-1"
        }
      ]
    },
    "sql": null,
    "promql": null,
    "promql_condition": null,
    "aggregation": {
      "group_by": ["label1", "label2"],
      "function": "avg",
      "having": {
        "column": "value",
        "operator": ">=",
        "value": 90,
        "ignore_case": false
      }
    },
    "vrl_function": null,
    "search_event_type": null,
    "multi_time_range": []
  },
  "trigger_condition": {
    "period": 5,
    "operator": ">=",
    "threshold": 1,
    "frequency": 1,
    "cron": "",
    "frequency_type": "minutes",
    "silence": 30,
    "timezone": "UTC"
  },
  "destinations": ["Slack Alertas"],
  "enabled": true,
  "description": "Descripción de la alerta"
}
```

## ⚠️ Errores Comunes

### "No conditions"
**Problema**: Los campos `column`, `operator`, `value` están vacíos en `conditions.items[]`

**Solución**: 
- Si no necesitas filtros, deja `items: []` vacío
- Si necesitas filtros, llena todos los campos correctamente

### "Invalid aggregation"
**Problema**: Falta el objeto `aggregation` o está mal configurado

**Solución**: Siempre incluye:
```json
"aggregation": {
  "group_by": ["campo"],
  "function": "avg",
  "having": {
    "column": "value",
    "operator": ">=",
    "value": 90
  }
}
```

### "Stream not found"
**Problema**: El `stream_name` no existe

**Solución**: Ve a Streams → Metrics y copia el nombre exacto del stream

## 📚 Recursos

- Ver streams disponibles: UI → Streams → Metrics
- Probar queries: UI → Logs/Metrics → Query Explorer
- Documentación: https://openobserve.ai/docs/

