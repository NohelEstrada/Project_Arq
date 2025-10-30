# Guía: Configurar Notificaciones de Slack en Grafana

## 📱 Paso 1: Crear Webhook de Slack

### 1.1. Ir a Slack API
Visita: https://api.slack.com/messaging/webhooks

### 1.2. Crear una nueva app
1. Click en "Create New App"
2. Selecciona "From scratch"
3. Nombre: `Pharmacy Monitoring`
4. Workspace: Selecciona tu workspace de Slack
5. Click "Create App"

### 1.3. Activar Incoming Webhooks
1. En el menú lateral, selecciona "Incoming Webhooks"
2. Activa el toggle "Activate Incoming Webhooks"
3. Scroll abajo y click en "Add New Webhook to Workspace"
4. Selecciona el canal donde quieres recibir alertas (ej: `#pharmacy-alerts`, `#unis-project`)
5. Click "Allow"

### 1.4. Copiar la Webhook URL
Verás algo como:
```
https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXX
```

**¡GUÁRDALA! La necesitarás en el siguiente paso.**

## 🔐 Paso 2: Configurar la Webhook URL

Tienes **2 opciones**:

### Opción A: Usar Variable de Entorno (RECOMENDADO - Más Seguro)

1. **Editar docker-compose.prod.yml**:
   ```bash
   nano docker-compose.prod.yml
   ```

2. **Agregar la variable de entorno** en la sección de Grafana:
   ```yaml
   grafana:
     image: grafana/grafana:latest
     container_name: grafana-prod
     environment:
       - GF_SECURITY_ADMIN_USER=admin
       - GF_SECURITY_ADMIN_PASSWORD=admin
       - SLACK_WEBHOOK_URL=https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXX
       # ... otras variables
   ```

3. **Actualizar alerting.yml** para usar la variable:
   ```yaml
   - uid: slack-unis
     type: slack
     settings:
       recipient: '#pharmacy-alerts'
       title: '🚨 Pharmacy System Alert'
       text: |-
         {{ range .Alerts }}
         *Alert:* {{ .Labels.alertname }}
         *Status:* {{ .Status }}
         *Severity:* {{ .Labels.severity }}
         *Summary:* {{ .Annotations.summary }}
         *Description:* {{ .Annotations.description }}
         {{ if .ValueString }}*Value:* {{ .ValueString }}{{ end }}
         {{ end }}
     secureSettings:
       url: ${SLACK_WEBHOOK_URL}  # ← Usa la variable de entorno
   ```

4. **Reiniciar Grafana**:
   ```bash
   docker-compose -f docker-compose.prod.yml up -d grafana
   ```

### Opción B: URL Directa en alerting.yml (Más Rápido pero menos seguro)

1. **Editar directamente alerting.yml**:
   ```bash
   nano monitoring/grafana/provisioning/alerting/alerting.yml
   ```

2. **Reemplazar la URL**:
   ```yaml
   secureSettings:
     url: 'https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXX'
   ```

3. **Reiniciar Grafana**:
   ```bash
   docker restart grafana-prod
   ```

## ✅ Paso 3: Aplicar Cambios

Ya he actualizado `alerting.yml` para usar **Email + Slack**. Solo necesitas:

1. **Configurar la Webhook URL** (Opción A o B arriba)

2. **Reiniciar Grafana**:
   ```bash
   cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy
   bash monitoring/reload-grafana-rules.sh
   ```

## 🧪 Paso 4: Probar la Integración

### Prueba Manual desde Slack:
```bash
curl -X POST https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXX \
  -H 'Content-Type: application/json' \
  -d '{
    "text": "🧪 Test de conexión desde terminal",
    "blocks": [
      {
        "type": "section",
        "text": {
          "type": "mrkdwn",
          "text": "*Alert:* Test Alert\n*Status:* Firing\n*Severity:* warning"
        }
      }
    ]
  }'
```

Si recibes `ok` en la respuesta, ¡funciona! ✅

### Prueba desde Grafana:

1. **Accede a Grafana**: http://localhost:3000

2. **Ve a Alerting > Contact points**

3. **Encuentra "Email + Slack"** y click en "Edit"

4. **Click en "Test"** para enviar una notificación de prueba

5. **Verifica tu canal de Slack**

## 🔍 Troubleshooting

### No recibo notificaciones en Slack

1. **Verificar logs de Grafana**:
   ```bash
   docker logs grafana-prod --tail 100 | grep -i slack
   ```

2. **Verificar que la Webhook URL sea correcta**:
   ```bash
   # Probar manualmente
   curl -X POST https://hooks.slack.com/services/TU_WEBHOOK_URL \
     -H 'Content-Type: application/json' \
     -d '{"text": "Test"}'
   ```

3. **Verificar que el canal existe**:
   - Asegúrate de que `#pharmacy-alerts` o tu canal exista en Slack
   - Si es canal privado, la app debe estar invitada al canal

4. **Ver configuración actual**:
   ```bash
   curl -s -u admin:admin http://localhost:3000/api/v1/provisioning/contact-points | jq
   ```

### Error: "invalid_payload"

- La Webhook URL está mal formateada
- Verifica que empiece con `https://hooks.slack.com/services/`

### Error: "channel_not_found"

- El canal `#pharmacy-alerts` no existe
- Cámbialo en `alerting.yml`:
  ```yaml
  recipient: '#general'  # o el canal que tengas
  ```

### Las alertas se envían pero no aparecen

- Verifica que estés viendo el canal correcto en Slack
- La app debe tener permisos en ese canal
- Para canales privados: `/invite @Pharmacy Monitoring` en el canal

## 📋 Configuración Actual

Tu archivo `alerting.yml` ahora tiene:

- **Contact Point**: "Email + Slack"
  - ✅ Email a: dnestrada@unis.edu.gt, jflores@unis.edu.gt
  - ⏳ Slack a: #pharmacy-alerts (pendiente configurar Webhook URL)

- **Políticas**:
  - **Critical**: Notifica inmediatamente, repite cada 30 min
  - **Warning**: Espera 30s, repite cada 4 horas

## 🎨 Personalizar Mensajes de Slack

Puedes personalizar el formato en `alerting.yml`:

```yaml
text: |-
  {{ range .Alerts }}
  🚨 *{{ .Labels.alertname }}*
  
  📊 *Estado:* {{ .Status }}
  ⚠️ *Severidad:* {{ .Labels.severity }}
  📝 *Resumen:* {{ .Annotations.summary }}
  📄 *Descripción:* {{ .Annotations.description }}
  {{ if .ValueString }}💯 *Valor:* {{ .ValueString }}{{ end }}
  
  🔗 <http://localhost:3000|Ver en Grafana>
  {{ end }}
```

## 🔄 Siguiente Paso

**Configura tu Webhook URL ahora:**

```bash
# Edita docker-compose.prod.yml
nano docker-compose.prod.yml

# Agrega en la sección grafana:
environment:
  - SLACK_WEBHOOK_URL=TU_WEBHOOK_URL_AQUI

# Guarda (Ctrl+O, Enter, Ctrl+X)

# Reinicia Grafana
docker-compose -f docker-compose.prod.yml up -d grafana

# Espera 10 segundos
sleep 10

# Verifica que funcione
bash monitoring/reload-grafana-rules.sh
```

## 📚 Referencias

- [Slack Incoming Webhooks](https://api.slack.com/messaging/webhooks)
- [Grafana Slack Notifications](https://grafana.com/docs/grafana/latest/alerting/manage-notifications/integrations/configure-slack/)
- [Slack Block Kit Builder](https://app.slack.com/block-kit-builder) - Para diseñar mensajes

