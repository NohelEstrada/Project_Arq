# 🚀 OpenObserve Alert Forwarder

Servicio intermediario que recibe alertas de Grafana y las reenvía como si fueran de OpenObserve.

## 📋 ¿Qué Hace?

1. **Recibe** alertas de Grafana vía webhook
2. **Reformatea** el mensaje para que parezca de OpenObserve
3. **Reenvía** a email y Slack sin el logo de Grafana

## 🔧 Arquitectura

```
Grafana → Webhook → Alert Forwarder → Email + Slack
                         (Puerto 5001)    (Como OpenObserve)
```

## 🚀 Iniciar el Servicio

### Opción 1: Con Docker (Recomendada)

```bash
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy/monitoring/openobserve

# Construir e iniciar
docker-compose -f docker-compose-forwarder.yml up -d --build

# Ver logs
docker logs -f openobserve-alert-forwarder

# Detener
docker-compose -f docker-compose-forwarder.yml down
```

### Opción 2: Directamente con Python

```bash
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy/monitoring/openobserve

# Instalar dependencias
pip3 install -r requirements.txt

# Ejecutar
python3 alert-forwarder.py
```

## ✅ Verificar que Funciona

### 1. Health Check
```bash
curl http://localhost:5001/health
```

Debe responder:
```json
{"status": "healthy"}
```

### 2. Probar Webhook Manualmente
```bash
curl -X POST http://localhost:5001/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "alerts": [{
      "status": "firing",
      "labels": {
        "alertname": "Test Alert",
        "severity": "critical"
      },
      "annotations": {
        "summary": "Test summary",
        "description": "Test description"
      },
      "startsAt": "2025-10-30T06:00:00Z"
    }]
  }'
```

## 🔄 Reiniciar Grafana

Después de iniciar el forwarder, reinicia Grafana para que reconozca el nuevo webhook:

```bash
docker restart grafana-prod
```

## 📊 Formato de Notificaciones OpenObserve

### Email:
```
Subject: [OpenObserve] Nombre Alerta - Severidad

****************************************************
*                                                  *
*        OPENOBSERVE ALERT NOTIFICATION           *
*                                                  *
****************************************************

ALERT INFORMATION
══════════════════════════════════════════════════
Alert Name    : [Nombre]
Status        : [Estado]
Severity      : [Criticidad]

METADATA
══════════════════════════════════════════════════
Organization  : default
Stream Type   : metrics
...
```

### Slack:
- Header: 📊 OpenObserve Alert
- Formato con bloques interactivos
- Botones: "View Alert", "Dashboard"
- Footer: "Powered by OpenObserve 🚀"

## ⚙️ Configuración

El servicio usa estas credenciales (ya configuradas en el código):

```python
SMTP_USER = "dnestrada@unis.edu.gt"
SMTP_PASSWORD = "tjkzeziklzmsjxmd"
SLACK_WEBHOOK = "https://hooks.slack.com/services/..."
EMAIL_RECIPIENTS = ["dnestrada@unis.edu.gt", "jflores@unis.edu.gt"]
```

## 🔍 Logs

Ver los logs del servicio:

```bash
# Con Docker
docker logs -f openobserve-alert-forwarder

# Directo con Python
# Los logs aparecen en la consola donde lo ejecutaste
```

## 🧪 Probar Alertas

1. Pon la alerta de CPU en umbral bajo (ej: 2%)
2. Reinicia Grafana: `docker restart grafana-prod`
3. Espera 2-3 minutos
4. Deberías recibir:
   - 1 email de Grafana (con logo de Grafana)
   - 1 email de "OpenObserve" (sin logo, formato OpenObserve)
   - 2 mensajes en Slack (#alertas)

## 📝 Endpoints

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/webhook` | POST | Recibe alertas de Grafana |
| `/health` | GET | Health check |

## 🛠️ Troubleshooting

### El servicio no inicia:
```bash
# Verificar que el puerto 5001 esté libre
lsof -i :5001

# Ver logs de error
docker logs openobserve-alert-forwarder
```

### Grafana no puede conectar:
```bash
# Verificar que estén en la misma red
docker network inspect pharmacy-network-prod

# Debería mostrar ambos contenedores:
# - grafana-prod
# - openobserve-alert-forwarder
```

### No llegan los emails:
- Verifica las credenciales SMTP en `alert-forwarder.py`
- Revisa los logs del forwarder
- Prueba manualmente con curl

### No llegan a Slack:
- Verifica el webhook URL en `alert-forwarder.py`
- Prueba el webhook directamente con curl

## 📦 Archivos Creados

- `alert-forwarder.py` - Servicio principal
- `requirements.txt` - Dependencias Python
- `Dockerfile.forwarder` - Imagen Docker
- `docker-compose-forwarder.yml` - Compose file
- `START-FORWARDER.md` - Esta guía

## 🔄 Flujo Completo

1. **Grafana** detecta alerta
2. Envía a **dos receivers**:
   - Email + Slack (normal de Grafana)
   - OpenObserve Forwarder (webhook)
3. **Forwarder** recibe el webhook
4. Reformatea como OpenObserve
5. Envía email y Slack sin logo de Grafana

## ✨ Ventajas

✅ Sin logo de Grafana en notificaciones OpenObserve
✅ Formato completamente personalizado
✅ Control total sobre el contenido
✅ Fácil de modificar y extender
✅ Logs detallados para debugging

