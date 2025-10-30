# 📧 Configuración de SMTP para Alertas por Email

## 🎯 Objetivo

Configurar Gmail SMTP para que Grafana pueda enviar alertas por correo electrónico a:
- dnestrada@unis.edu.gt
- jflores@unis.edu.gt

---

## 🔐 Paso 1: Crear Contraseña de Aplicación en Gmail

### 1. Habilitar Verificación en 2 Pasos

1. Ve a tu cuenta de Gmail: https://myaccount.google.com/
2. En el menú lateral, selecciona **"Seguridad"**
3. Busca **"Verificación en 2 pasos"**
4. Si no está habilitada, haz clic en **"Activar"** y sigue los pasos

### 2. Generar Contraseña de Aplicación

1. En la misma página de Seguridad, busca **"Contraseñas de aplicaciones"**
   - Si no la ves, asegúrate de que la verificación en 2 pasos esté habilitada
2. Haz clic en **"Contraseñas de aplicaciones"**
3. En el menú desplegable:
   - **Seleccionar app**: "Correo"
   - **Seleccionar dispositivo**: "Otro (nombre personalizado)"
   - Escribe: "Pharmacy Monitoring System"
4. Haz clic en **"Generar"**
5. **¡IMPORTANTE!** Copia la contraseña generada (son 16 caracteres en formato: `abcd efgh ijkl mnop`)
   - **Guárdala en un lugar seguro**, no podrás verla de nuevo
   - Ejemplo: `xmfk pqrs tuvw xyza`

---

## ⚙️ Paso 2: Actualizar Configuración

### Opción A: Variables de Entorno (Recomendado)

Edita `docker-compose.prod.yml`:

```yaml
grafana:
  environment:
    - GF_SMTP_ENABLED=true
    - GF_SMTP_HOST=smtp.gmail.com:587
    - GF_SMTP_USER=tu-email@gmail.com  # ⬅️ CAMBIA ESTO
    - GF_SMTP_PASSWORD=xmfk pqrs tuvw xyza  # ⬅️ PEGA TU CONTRASEÑA AQUÍ
    - GF_SMTP_FROM_ADDRESS=tu-email@gmail.com  # ⬅️ CAMBIA ESTO
    - GF_SMTP_FROM_NAME=Pharmacy Monitoring System
```

### Opción B: Archivo grafana.ini

Edita `monitoring/grafana/grafana.ini`:

```ini
[smtp]
enabled = true
host = smtp.gmail.com:587
user = tu-email@gmail.com  # ⬅️ CAMBIA ESTO
password = xmfk pqrs tuvw xyza  # ⬅️ PEGA TU CONTRASEÑA AQUÍ
skip_verify = false
from_address = tu-email@gmail.com  # ⬅️ CAMBIA ESTO
from_name = Pharmacy Monitoring System
ehlo_identity = pharmacy-grafana
```

---

## 🔄 Paso 3: Reiniciar Grafana

```bash
cd ensurancePharmacy
docker-compose -f docker-compose.prod.yml restart grafana
```

Espera 10 segundos para que Grafana reinicie completamente.

---

## ✅ Paso 4: Probar Configuración

### Método 1: Desde la UI de Grafana

1. Accede a Grafana: http://localhost:3000
2. Login: `admin` / `admin`
3. Ve a **Alerting** → **Contact points**
4. Busca **"Email Notifications"**
5. Haz clic en **"Edit"** (icono de lápiz)
6. Baja hasta el final y haz clic en **"Test"**
7. Deberías ver: ✅ "Test notification sent"
8. **Revisa tu bandeja de entrada** en dnestrada@unis.edu.gt y jflores@unis.edu.gt

### Método 2: Desde la Terminal

```bash
# Ver logs de Grafana para verificar SMTP
docker logs grafana-prod | grep smtp

# Deberías ver algo como:
# logger=ngalert.notifier.email t=... msg="Successfully sent notification"
```

### Método 3: Forzar una Alerta

```bash
# Generar carga para activar alerta de CPU
cd jmeter-tests
./run-stress-test.sh 100 120 8101
```

Después de 5 minutos con CPU alta, deberías recibir un email.

---

## 🐛 Troubleshooting

### Error: "Authentication failed"

**Causa**: Contraseña incorrecta o verificación en 2 pasos no habilitada

**Solución**:
1. Verifica que usas una **contraseña de aplicación** (16 caracteres), NO tu contraseña normal de Gmail
2. Verifica que la verificación en 2 pasos esté habilitada
3. Genera una nueva contraseña de aplicación si es necesario

### Error: "Could not connect to SMTP server"

**Causa**: Puerto o host incorrectos

**Solución**:
```ini
host = smtp.gmail.com:587  # ⬅️ Asegúrate que sea :587
```

### Error: "From address not verified"

**Causa**: El email "from" no coincide con tu cuenta de Gmail

**Solución**:
```ini
user = tu-email@gmail.com
from_address = tu-email@gmail.com  # ⬅️ Debe ser el mismo
```

### No recibo emails pero no hay errores

1. **Revisa la carpeta de SPAM** en tu correo
2. Verifica los destinatarios en `monitoring/grafana/provisioning/alerting/alerting.yml`:
   ```yaml
   settings:
     addresses: dnestrada@unis.edu.gt;jflores@unis.edu.gt
   ```
3. Reinicia Grafana después de cambios:
   ```bash
   docker-compose -f docker-compose.prod.yml restart grafana
   ```

---

## 🔐 Seguridad

### ¿Es seguro usar contraseña de aplicación?

✅ **Sí**, las contraseñas de aplicación son:
- Específicas para una aplicación
- No dan acceso a tu cuenta completa de Gmail
- Puedes revocarlas en cualquier momento sin afectar tu contraseña principal
- Se pueden regenerar si se comprometen

### ¿Cómo revocar una contraseña de aplicación?

1. Ve a https://myaccount.google.com/security
2. **Contraseñas de aplicaciones**
3. Busca "Pharmacy Monitoring System"
4. Haz clic en **"Eliminar"**

---

## 📝 Ejemplo de Configuración Completa

### docker-compose.prod.yml
```yaml
grafana:
  image: grafana/grafana:latest
  container_name: grafana-prod
  environment:
    - GF_SMTP_ENABLED=true
    - GF_SMTP_HOST=smtp.gmail.com:587
    - GF_SMTP_USER=monitoring@example.com
    - GF_SMTP_PASSWORD=abcd efgh ijkl mnop
    - GF_SMTP_FROM_ADDRESS=monitoring@example.com
    - GF_SMTP_FROM_NAME=Pharmacy Monitoring
    - GF_UNIFIED_ALERTING_ENABLED=true
```

### monitoring/grafana/provisioning/alerting/alerting.yml
```yaml
contactPoints:
  - orgId: 1
    name: Email Notifications
    receivers:
      - uid: email-unis
        type: email
        settings:
          addresses: dnestrada@unis.edu.gt;jflores@unis.edu.gt
          singleEmail: false
```

---

## 🎓 Alternativas a Gmail

Si prefieres usar otro proveedor de email:

### Outlook/Office 365
```ini
host = smtp.office365.com:587
user = tu-email@outlook.com
password = tu_contraseña
```

### SendGrid
```ini
host = smtp.sendgrid.net:587
user = apikey
password = TU_API_KEY_DE_SENDGRID
```

### Mailgun
```ini
host = smtp.mailgun.org:587
user = postmaster@tu-dominio.mailgun.org
password = TU_PASSWORD_DE_MAILGUN
```

---

## ✅ Checklist

- [ ] Verificación en 2 pasos habilitada en Gmail
- [ ] Contraseña de aplicación generada
- [ ] Contraseña copiada y guardada
- [ ] `docker-compose.prod.yml` actualizado con tu email y contraseña
- [ ] `grafana.ini` actualizado (opcional)
- [ ] Grafana reiniciado
- [ ] Test de email enviado desde Grafana
- [ ] Email de prueba recibido en dnestrada@unis.edu.gt
- [ ] Email de prueba recibido en jflores@unis.edu.gt

---

## 📞 ¿Necesitas Ayuda?

Si tienes problemas configurando SMTP:

1. Revisa los logs: `docker logs grafana-prod | grep smtp`
2. Verifica el [troubleshooting](#-troubleshooting) arriba
3. Lee la documentación completa en [ALERTAS-README.md](./ALERTAS-README.md)

**¡Una vez configurado, las alertas se enviarán automáticamente!** 🎉

