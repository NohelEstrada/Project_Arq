# 📧 Guía: Arreglar Notificaciones por Email

## ✅ Estado Actual
- ✅ Slack: Funcionando
- ❌ Email: Credenciales inválidas

## 🔐 Solución: Generar Nueva Contraseña de Aplicación

Gmail requiere una **Contraseña de Aplicación** específica (NO tu contraseña normal).

### Paso 1: Ir a Google Account
Abre: https://myaccount.google.com/apppasswords

> **Nota**: Si el enlace no funciona, ve a:
> 1. https://myaccount.google.com/
> 2. Click en "Seguridad" (menú lateral)
> 3. Busca "Contraseñas de aplicaciones" o "App Passwords"

### Paso 2: Activar Verificación en Dos Pasos (si no está activa)

Si ves un mensaje que dice "No disponible", necesitas activar la verificación en dos pasos primero:

1. Ve a: https://myaccount.google.com/signinoptions/two-step-verification
2. Click en "Empezar" o "Get Started"
3. Sigue los pasos para activarla (puedes usar tu teléfono)

### Paso 3: Crear Contraseña de Aplicación

Una vez en la página de Contraseñas de Aplicaciones:

1. **Selecciona la app**: "Correo" o "Mail"
2. **Selecciona el dispositivo**: "Otro (nombre personalizado)"
3. **Escribe un nombre**: `Grafana Pharmacy Monitoring`
4. Click en **"Generar"**

Verás una contraseña de 16 caracteres como:
```
abcd efgh ijkl mnop
```

5. **COPIA ESTA CONTRASEÑA** (sin los espacios)

---

## 🔧 Paso 4: Actualizar Docker Compose

1. **Abrir el archivo**:
```bash
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy
nano docker-compose.prod.yml
```

2. **Buscar la línea** (usa Ctrl+W para buscar):
```yaml
- GF_SMTP_PASSWORD=mkwibcfkcaqtfdpy
```

3. **Reemplazarla con** (SIN espacios):
```yaml
- GF_SMTP_PASSWORD=abcdefghijklmnop
```
(Reemplaza con tu contraseña de 16 caracteres)

4. **Guardar** (Ctrl+O, Enter, Ctrl+X)

---

## 🔄 Paso 5: Reiniciar Grafana

```bash
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy

# Detener y eliminar contenedor actual
docker stop grafana-prod
docker rm grafana-prod

# Recrear con nueva configuración
docker-compose -f docker-compose.prod.yml up -d grafana

# Esperar 15 segundos
sleep 15
```

---

## ✅ Paso 6: Verificar que Funcione

```bash
# Ver logs en tiempo real
docker logs -f grafana-prod | grep -i email
```

**Busca estos mensajes:**

✅ **Si funciona**, verás:
```
Successfully sent email
```

❌ **Si aún falla**, verás:
```
failed to send email: 535
```

---

## 🧪 Probar Manualmente

Si quieres probar el SMTP directamente desde terminal:

```bash
# Instalar swaks si no lo tienes
brew install swaks  # en Mac
# apt-get install swaks  # en Linux

# Probar conexión SMTP
swaks --to dnestrada@unis.edu.gt \
  --from pharmacy.monitoring@gmail.com \
  --server smtp.gmail.com:587 \
  --auth LOGIN \
  --auth-user pharmacy.monitoring@gmail.com \
  --auth-password "TU_CONTRASENA_DE_16_CARACTERES" \
  --tls \
  --header "Subject: Test Grafana" \
  --body "Test de conexión SMTP"
```

Si este comando funciona, el problema está en Grafana. Si falla, el problema está en las credenciales.

---

## 🆘 Alternativas si Gmail no Funciona

### Opción A: Usar tu Email Personal

Si `pharmacy.monitoring@gmail.com` no funciona, puedes usar tu email personal:

```yaml
- GF_SMTP_USER=tu_email_personal@gmail.com
- GF_SMTP_PASSWORD=tu_contrasena_de_aplicacion
- GF_SMTP_FROM_ADDRESS=tu_email_personal@gmail.com
```

### Opción B: Usar otro Proveedor SMTP

**SendGrid** (gratis hasta 100 emails/día):
```yaml
- GF_SMTP_ENABLED=true
- GF_SMTP_HOST=smtp.sendgrid.net:587
- GF_SMTP_USER=apikey
- GF_SMTP_PASSWORD=tu_api_key_de_sendgrid
- GF_SMTP_FROM_ADDRESS=pharmacy@tudominio.com
```

**Mailgun** (gratis hasta 5,000 emails/mes):
```yaml
- GF_SMTP_ENABLED=true
- GF_SMTP_HOST=smtp.mailgun.org:587
- GF_SMTP_USER=postmaster@tu-dominio.mailgun.org
- GF_SMTP_PASSWORD=tu_password_mailgun
- GF_SMTP_FROM_ADDRESS=alerts@tu-dominio.mailgun.org
```

---

## 📝 Troubleshooting

### Error: "App passwords are not available"

**Causa**: La cuenta no tiene verificación en dos pasos activada.

**Solución**: 
1. Ve a https://myaccount.google.com/signinoptions/two-step-verification
2. Actívala
3. Luego podrás crear contraseñas de aplicaciones

### Error: "Please log in via your web browser"

**Causa**: Gmail detectó actividad sospechosa.

**Solución**:
1. Ve a https://accounts.google.com/DisplayUnlockCaptcha
2. Click en "Continue"
3. Intenta de nuevo

### Error: "Less secure app access"

**Causa**: Gmail bloqueó apps menos seguras.

**Solución**:
- Ya no uses "Permitir apps menos seguras" (obsoleto)
- DEBES usar Contraseñas de Aplicaciones

---

## 🎯 Comando Rápido de Verificación

Después de reiniciar Grafana, ejecuta:

```bash
# Ver si se envió correctamente
docker logs grafana-prod --tail 100 | grep -i "email"

# Ver alertas activas
curl -s -u admin:admin http://localhost:3000/api/alertmanager/grafana/api/v2/alerts | grep -o '"alertname":"[^"]*"' | head -5
```

---

## ✨ Resumen Rápido

1. Ve a: https://myaccount.google.com/apppasswords
2. Crea contraseña de aplicación: `Grafana Pharmacy Monitoring`
3. Copia la contraseña de 16 caracteres
4. Edita `docker-compose.prod.yml`:
   ```yaml
   - GF_SMTP_PASSWORD=tu_nueva_contrasena
   ```
5. Reinicia:
   ```bash
   docker stop grafana-prod && docker rm grafana-prod
   docker-compose -f docker-compose.prod.yml up -d grafana
   ```
6. Verifica logs:
   ```bash
   docker logs -f grafana-prod | grep email
   ```

---

**¿Necesitas ayuda?** Revisa los logs con:
```bash
docker logs grafana-prod --tail 50 | grep -i smtp
```

