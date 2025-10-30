# 🔧 Solución: Notificaciones No Se Envían

## 📊 Estado Actual
✅ **Alertas funcionando**: Las reglas se están evaluando y activando correctamente  
❌ **Slack**: Error de conexión (EOF)  
❌ **Email**: Credenciales inválidas

---

## 🚀 SOLUCIÓN RÁPIDA - Opción 1: Probar Webhook de Slack

### Paso 1: Verificar que la Webhook funcione
```bash
curl -X POST https://hooks.slack.com/services/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 \
  -H 'Content-Type: application/json' \
  -d '{"text": "🧪 Test desde terminal"}'
```

**Si recibes `ok`** → Webhook funciona, el problema es la configuración de Grafana  
**Si recibes error** → Necesitas regenerar la Webhook

### Paso 2: Regenerar Webhook de Slack (si es necesario)

1. Ve a: https://api.slack.com/apps
2. Selecciona tu app "Pharmacy Monitoring" (o la que creaste)
3. Ve a "Incoming Webhooks"
4. Si ves webhooks antiguos, desactívalos
5. Click en "Add New Webhook to Workspace"
6. Selecciona el canal `#alertas`
7. Copia la nueva URL

8. **Actualizar en docker-compose.prod.yml**:
```bash
nano docker-compose.prod.yml
```

Busca y reemplaza:
```yaml
- SLACK_WEBHOOK_URL=TU_NUEVA_URL_AQUI
```

9. **Reiniciar Grafana**:
```bash
docker stop grafana-prod
docker rm grafana-prod
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy
docker-compose -f docker-compose.prod.yml up -d grafana
```

---

## 📧 SOLUCIÓN - Opción 2: Arreglar Email (Gmail)

### El problema: Contraseña de aplicación inválida

Gmail requiere una **Contraseña de Aplicación** específica (no tu contraseña normal).

### Paso 1: Generar Nueva Contraseña de Aplicación

1. **Ir a tu cuenta de Google**: https://myaccount.google.com/

2. **Ir a Seguridad** → **Verificación en dos pasos**
   - Si no está activa, actívala primero

3. **Contraseñas de aplicaciones**: https://myaccount.google.com/apppasswords

4. **Crear nueva contraseña**:
   - Nombre: `Grafana Pharmacy Monitoring`
   - Click "Crear"
   - **Copia la contraseña de 16 caracteres** (sin espacios)

### Paso 2: Actualizar en Docker Compose

```bash
nano docker-compose.prod.yml
```

Busca la línea:
```yaml
- GF_SMTP_PASSWORD=mkwibcfkcaqtfdpy
```

Reemplázala con:
```yaml
- GF_SMTP_PASSWORD=TU_NUEVA_CONTRASENA_DE_16_CARACTERES
```

### Paso 3: Reiniciar Grafana
```bash
docker stop grafana-prod
docker rm grafana-prod
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy
docker-compose -f docker-compose.prod.yml up -d grafana
```

---

## 🧪 SOLUCIÓN TEMPORAL - Opción 3: Probar con Gmail Personal

Si el email pharmacy.monitoring@gmail.com no funciona, puedes usar tu email personal:

```bash
nano docker-compose.prod.yml
```

Cambia:
```yaml
- GF_SMTP_USER=tu_email@gmail.com
- GF_SMTP_PASSWORD=tu_contrasena_de_aplicacion
- GF_SMTP_FROM_ADDRESS=tu_email@gmail.com
```

---

## ✅ Verificar que Funcione

### 1. Ver logs en tiempo real:
```bash
docker logs -f grafana-prod | grep -i -E "(slack|email|smtp|send)"
```

### 2. Forzar una alerta de prueba:

La regla de CPU ya está en 3% (siempre activa). Espera 2 minutos y verifica:

```bash
# Ver alertas activas
curl -s -u admin:admin http://localhost:3000/api/alertmanager/grafana/api/v2/alerts | grep -i "cpu"

# Ver si se enviaron notificaciones
docker logs grafana-prod --tail 50 | grep -i "successfully sent"
```

### 3. Si ves estos mensajes = ✅ FUNCIONA:
```
Successfully sent Slack message
Successfully sent email
```

### 4. Si ves estos mensajes = ❌ AÚN HAY ERROR:
```
Failed to send Slack message
failed to send email: 535
```

---

## 🎯 RECOMENDACIÓN URGENTE

**Empieza por Slack** (es más fácil y rápido):

1. Probar la webhook actual con curl (arriba)
2. Si falla, regenerar webhook
3. Actualizar docker-compose.prod.yml
4. Reiniciar Grafana
5. Verificar logs

**Luego Email**:
1. Generar nueva contraseña de aplicación
2. Actualizar docker-compose.prod.yml
3. Reiniciar Grafana
4. Verificar logs

---

## 📝 Script Rápido de Diagnóstico

```bash
#!/bin/bash
echo "=== DIAGNÓSTICO DE NOTIFICACIONES ==="
echo ""
echo "1. Probando Webhook de Slack..."
curl -X POST https://hooks.slack.com/services/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 \
  -H 'Content-Type: application/json' \
  -d '{"text": "Test"}' 2>&1

echo ""
echo "2. Alertas activas en Grafana:"
curl -s -u admin:admin http://localhost:3000/api/alertmanager/grafana/api/v2/alerts 2>&1 | grep -o '"alertname":"[^"]*"' | head -5

echo ""
echo "3. Últimos errores de notificación:"
docker logs grafana-prod --tail 100 2>&1 | grep -i "failed" | tail -5

echo ""
echo "=== FIN DEL DIAGNÓSTICO ==="
```

Guarda esto como `monitoring/diagnostico-notificaciones.sh` y ejecútalo con:
```bash
bash monitoring/diagnostico-notificaciones.sh
```

---

## 🆘 Si Nada Funciona

**Opción alternativa**: Usar solo Email con otro proveedor (ej: SendGrid, Mailgun) o configurar un servidor SMTP propio.

O **temporalmente**, deshabilitar Slack y dejar solo Email hasta resolverlo:

```yaml
# En alerting.yml, cambiar de:
receiver: Email + Slack

# A:
receiver: Email Notifications  # Solo email
```

