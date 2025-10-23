# 🚨 Sistema de Alertas - Pharmacy Monitoring

## 📋 Resumen

Este sistema implementa alertas automáticas para **Grafana** y **OpenObserve** que envían notificaciones por **correo electrónico** y **Slack** cuando se detectan problemas en el sistema.

### 📧 Destinatarios de Notificaciones

- **Correos**: dnestrada@unis.edu.gt, jflores@unis.edu.gt
- **Slack**: Canal #unis-project

### 🎯 Tipos de Alertas

| Alerta | Condición | Duración | Severidad | Canal |
|--------|-----------|----------|-----------|-------|
| **CPU Alta** | CPU > 70% | 5 minutos | ⚠️ Warning | Email + Slack |
| **CPU Crítica** | CPU > 90% | 2 minutos | 🔴 Critical | Email + Slack |
| **Memoria Alta** | Memoria > 80% | 5 minutos | ⚠️ Warning | Email + Slack |
| **Memoria Crítica** | Memoria > 90% | 2 minutos | 🔴 Critical | Email + Slack |
| **Disco Alto** | Disco > 85% | 5 minutos | ⚠️ Warning | Email + Slack |
| **Response Time Alto** | Tiempo > 1000ms | 3 minutos | ⚠️ Warning | Email + Slack |
| **Error Rate Alto** | Errores > 5% | 3 minutos | ⚠️ Warning | Email + Slack |
| **Pipeline Fallido** | Build falla | 1 minuto | 🔴 Critical | Email + Slack |
| **Pipeline Lento** | Duración > 10min | 2 minutos | ⚠️ Warning | Email + Slack |
| **Cola Pipeline** | Queue > 5 jobs | 5 minutos | ⚠️ Warning | Email + Slack |

---

## 🚀 Configuración Inicial

### Prerequisitos

1. Docker y Docker Compose instalados
2. Servicios de monitoreo levantados (Prometheus, Grafana, OpenObserve)
3. Acceso a correo Gmail para SMTP (o configurar otro proveedor)
4. Webhook de Slack configurado

### Paso 1: Configurar SMTP para Grafana

Para que Grafana pueda enviar correos, necesitas configurar SMTP:

#### Opción A: Usar Gmail (Recomendado)

1. **Crear una contraseña de aplicación en Gmail**:
   - Ve a https://myaccount.google.com/security
   - Habilita "Verificación en 2 pasos" si no está habilitada
   - Ve a "Contraseñas de aplicaciones"
   - Genera una nueva contraseña para "Correo" en "Otra aplicación"
   - Copia la contraseña generada (ej: `abcd efgh ijkl mnop`)

2. **Actualizar configuración en docker-compose.prod.yml**:
   ```yaml
   - GF_SMTP_PASSWORD=abcd efgh ijkl mnop  # Tu contraseña de aplicación
   ```

3. **Actualizar configuración en grafana.ini**:
   ```ini
   [smtp]
   password = abcd efgh ijkl mnop  # Tu contraseña de aplicación
   ```

#### Opción B: Usar otro proveedor SMTP

Edita `monitoring/grafana/grafana.ini` y actualiza:

```ini
[smtp]
host = smtp.tu-proveedor.com:587
user = tu-email@dominio.com
password = tu_contraseña
from_address = tu-email@dominio.com
```

### Paso 2: Verificar Webhook de Slack

El webhook de Slack está configurado en el archivo `.env`:
```bash
# Ver el webhook configurado
cat monitoring/.env | grep SLACK_WEBHOOK_URL
```

**Canal**: #unis-project

### Paso 3: Levantar Servicios con Alertas

```bash
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy

# Detener servicios actuales
docker-compose -f docker-compose.prod.yml down

# Levantar con nueva configuración
docker-compose -f docker-compose.prod.yml up -d

# Verificar que los servicios estén corriendo
docker-compose -f docker-compose.prod.yml ps
```

### Paso 4: Verificar Configuración de Alertas en Grafana

1. Accede a Grafana: http://localhost:3000
2. Login: admin / admin
3. Ve a **Alerting** → **Contact points**
4. Verifica que aparezcan:
   - ✅ Email Notifications (dnestrada@unis.edu.gt, jflores@unis.edu.gt)
   - ✅ Slack Notifications (#unis-project)
   - ✅ Email + Slack (ambos canales)

5. Ve a **Alerting** → **Alert rules**
6. Verifica que aparezcan todas las reglas (10 alertas en total)

### Paso 5: Configurar Alertas en OpenObserve

```bash
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy/monitoring/openobserve

# Ejecutar script de configuración
./setup-alerts.sh
```

El script creará automáticamente todas las alertas en OpenObserve.

---

## 📊 Alertas Configuradas

### 🖥️ Alertas de Sistema

#### 1. CPU Usage - Warning
- **Condición**: CPU > 70%
- **Duración**: 5 minutos continuos
- **Severidad**: ⚠️ Warning
- **Acción**: Investigar causa del alto uso de CPU

**Ejemplo de notificación**:
```
🟡 HIGH CPU USAGE WARNING

Severity: Warning
Metric: CPU Usage
Current Value: 75.3%
Threshold: 70%
Duration: 5 minutes
Time: 2025-10-22 14:30:00

Action Required: Please investigate the cause of high CPU usage.
```

#### 2. CPU Usage - Critical
- **Condición**: CPU > 90%
- **Duración**: 2 minutos continuos
- **Severidad**: 🔴 Critical
- **Acción**: Acción inmediata requerida

**Ejemplo de notificación**:
```
🔴 CRITICAL CPU USAGE ALERT

Severity: CRITICAL
Metric: CPU Usage
Current Value: 93.7%
Threshold: 90%
Duration: 2 minutes
Time: 2025-10-22 14:35:00

⚠️ IMMEDIATE ACTION REQUIRED
The system CPU usage is critically high. Investigate immediately to prevent service degradation.
```

#### 3. Memory Usage - Warning
- **Condición**: Memoria > 80%
- **Duración**: 5 minutos continuos
- **Severidad**: ⚠️ Warning
- **Acción**: Monitorear uso de memoria

#### 4. Memory Usage - Critical
- **Condición**: Memoria > 90%
- **Duración**: 2 minutos continuos
- **Severidad**: 🔴 Critical
- **Acción**: Riesgo de OOM (Out of Memory)

#### 5. Disk Usage - Warning
- **Condición**: Disco > 85%
- **Duración**: 5 minutos continuos
- **Severidad**: ⚠️ Warning
- **Acción**: Limpiar espacio en disco

---

### 🌐 Alertas de Aplicación

#### 6. High Response Time
- **Condición**: Response Time (P95) > 1000ms
- **Duración**: 3 minutos continuos
- **Severidad**: ⚠️ Warning
- **Acción**: Degradación de performance detectada

#### 7. High Error Rate
- **Condición**: Tasa de errores HTTP 5xx > 5%
- **Duración**: 3 minutos continuos
- **Severidad**: ⚠️ Warning
- **Acción**: Revisar logs de la aplicación

---

### 🔧 Alertas de Pipeline

#### 8. Pipeline Build Failed
- **Condición**: Build falla
- **Duración**: 1 minuto
- **Severidad**: 🔴 Critical
- **Acción**: Revisar consola de Jenkins

**Ejemplo de notificación**:
```
🔴 PIPELINE BUILD FAILED

Severity: CRITICAL
Metric: Jenkins Build Status
Failures: 1
Time: 2025-10-22 15:00:00

⚠️ ACTION REQUIRED
The CI/CD pipeline has failed. Check Jenkins console output for details.
```

#### 9. Pipeline Taking Too Long
- **Condición**: Duración > 10 minutos
- **Duración**: 2 minutos
- **Severidad**: ⚠️ Warning
- **Acción**: Verificar performance del pipeline

#### 10. High Pipeline Queue
- **Condición**: Queue > 5 trabajos
- **Duración**: 5 minutos
- **Severidad**: ⚠️ Warning
- **Acción**: Considerar agregar más build agents

---

## 🧪 Pruebas de Alertas

### Probar Alerta de CPU

Para generar carga de CPU y activar la alerta:

```bash
# Opción 1: Usar stress test de JMeter
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy/jmeter-tests
./run-stress-test.sh 100 120 8101

# Opción 2: Usar stress test de K6
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy/k6-tests
k6 run --vus 100 --duration 2m pharmacy-stress-test.js
```

**Resultado esperado**:
- Después de 5 minutos con CPU > 70%: Alerta Warning
- Después de 2 minutos con CPU > 90%: Alerta Critical
- Notificaciones enviadas a email y Slack

### Probar Alerta de Pipeline

Para probar las alertas del pipeline:

```bash
# Hacer un cambio que falle el build
echo "error syntax" >> backv5/src/main/java/Error.java
git add .
git commit -m "Test: Trigger pipeline failure"
git push
```

**Resultado esperado**:
- Pipeline falla en Jenkins
- Después de 1 minuto: Alerta Critical enviada

### Verificar que llegaron las notificaciones

1. **Email**: Revisa las bandejas de entrada de:
   - dnestrada@unis.edu.gt
   - jflores@unis.edu.gt

2. **Slack**: Revisa el canal #unis-project

---

## 📈 Monitoreo de Alertas

### En Grafana

1. **Ver estado de alertas**:
   - Ve a **Alerting** → **Alert rules**
   - Estado: 🟢 Normal | 🔴 Alerting | ⚠️ Pending

2. **Ver historial de alertas**:
   - Ve a **Alerting** → **Silences**
   - Historial de cuándo se activaron y resolvieron

3. **Ver dashboards con alertas**:
   - **Application Performance Dashboard**: Alertas de CPU y Memoria visibles
   - **Pipeline Performance Dashboard**: Alertas de pipeline visibles

### En OpenObserve

1. Accede a: http://localhost:5080
2. Ve a **Alerts** en el menú lateral
3. Ver alertas activas y su estado
4. Ver historial de alertas disparadas

---

## 🔧 Troubleshooting

### Las alertas no se están enviando

#### Problema 1: SMTP no configurado correctamente

**Síntomas**: No llegan correos

**Solución**:
```bash
# Ver logs de Grafana
docker logs grafana-prod | grep smtp

# Probar envío de correo desde Grafana
# Ve a Grafana → Alerting → Contact points → Email → Test
```

Si ves error "Authentication failed":
- Verifica que usas una contraseña de aplicación de Gmail (no tu contraseña normal)
- Verifica que la verificación en 2 pasos esté habilitada

#### Problema 2: Slack webhook no funciona

**Síntomas**: No llegan mensajes a Slack

**Solución**:
```bash
# Cargar webhook desde .env
source monitoring/.env

# Probar webhook manualmente
curl -X POST $SLACK_WEBHOOK_URL \
  -H 'Content-Type: application/json' \
  -d '{"text": "Test de alerta del sistema Pharmacy"}'
```

Si el mensaje no llega:
- Verifica que el webhook sea válido en Slack
- Verifica que el canal #unis-project exista
- Regenera el webhook si es necesario

#### Problema 3: Las alertas no se activan

**Síntomas**: Condiciones se cumplen pero no hay notificación

**Solución**:
```bash
# Verificar que Prometheus tenga las métricas
curl http://localhost:9090/api/v1/query?query=node_cpu_seconds_total

# Verificar en Grafana
# Ve a Alerting → Alert rules → [Selecciona alerta] → "State history"
```

#### Problema 4: Alertas se envían constantemente

**Síntomas**: Recibes muchas notificaciones del mismo problema

**Solución**:
Las alertas están configuradas con `repeat_interval: 4h` (4 horas).

Si quieres cambiar esto:
1. Edita `monitoring/grafana/provisioning/alerting/alerting.yml`
2. Modifica `repeat_interval` a tu preferencia (ej: `1h`, `30m`)
3. Reinicia Grafana:
   ```bash
   docker-compose -f docker-compose.prod.yml restart grafana
   ```

---

## ⚙️ Configuración Avanzada

### Silenciar Alertas Temporalmente

Si necesitas silenciar una alerta (ej: durante mantenimiento):

1. Ve a Grafana → **Alerting** → **Silences**
2. Click en **"New Silence"**
3. Configura:
   - **Matcher**: Selecciona la alerta (ej: `alertname=cpu_warning_alert`)
   - **Duration**: Cuánto tiempo silenciar (ej: 2h)
   - **Creator**: Tu nombre
   - **Comment**: Razón del silencio
4. Click en **"Create"**

### Agregar Más Destinatarios

Para agregar más correos o canales de Slack:

1. Edita `monitoring/grafana/provisioning/alerting/alerting.yml`
2. Agrega más correos:
   ```yaml
   settings:
     addresses: dnestrada@unis.edu.gt;jflores@unis.edu.gt;nuevo@email.com
   ```
3. Reinicia Grafana

### Crear Alertas Personalizadas

#### En Grafana:

1. Ve a **Alerting** → **Alert rules** → **"New alert rule"**
2. Configura:
   - **Query**: Métrica de Prometheus
   - **Condition**: Umbral (threshold)
   - **Evaluate**: Frecuencia de evaluación
   - **For**: Duración antes de alertar
3. Asigna a un Contact Point
4. Guarda

#### En OpenObserve:

1. Ve a **Alerts** → **"Create Alert"**
2. Configura query y condiciones
3. Agrega destinos (email, Slack)
4. Guarda

---

## 📚 Estructura de Archivos

```
monitoring/
├── ALERTAS-README.md                           # Este archivo
├── grafana/
│   ├── grafana.ini                             # Config de Grafana con SMTP
│   ├── provisioning/
│   │   ├── alerting/
│   │   │   ├── alerting.yml                    # Contact points y políticas
│   │   │   └── rules.yml                       # Reglas de alertas
│   │   ├── datasources/
│   │   │   └── prometheus.yml                  # Datasource de Prometheus
│   │   └── dashboards/
│   │       └── dashboards.yml                  # Provisioning de dashboards
│   └── dashboards/
│       ├── application-performance.json         # Dashboard con alertas visuales
│       └── pipeline-performance.json            # Dashboard con alertas visuales
└── openobserve/
    ├── alerts-config.json                       # Config de alertas OpenObserve
    └── setup-alerts.sh                          # Script de configuración
```

---

## 🎯 Checklist de Configuración

### Grafana Alertas

- [ ] SMTP configurado en `grafana.ini`
- [ ] Password de aplicación de Gmail configurada
- [ ] Webhook de Slack configurado
- [ ] Servicios levantados con `docker-compose up -d`
- [ ] Contact Points visibles en Grafana
- [ ] Alert Rules visibles en Grafana (10 reglas)
- [ ] Test de email exitoso desde Contact Points
- [ ] Test de Slack exitoso desde Contact Points

### OpenObserve Alertas

- [ ] OpenObserve levantado y accesible
- [ ] Script `setup-alerts.sh` ejecutado
- [ ] Alertas visibles en OpenObserve (10 alertas)
- [ ] Prometheus enviando métricas a OpenObserve

### Pruebas

- [ ] Stress test ejecutado
- [ ] Alerta de CPU activada
- [ ] Email recibido
- [ ] Mensaje de Slack recibido
- [ ] Pipeline fallido probado
- [ ] Alerta de pipeline recibida

---

## 📞 Contactos y Soporte

### Destinatarios de Alertas

- **Daniel Estrada**: dnestrada@unis.edu.gt
- **Jorge Flores**: jflores@unis.edu.gt

### Canal de Slack

- **#unis-project**: https://unis-workspace.slack.com/archives/unis-project

### Accesos

- **Grafana**: http://localhost:3000 (admin/admin)
- **OpenObserve**: http://localhost:5080 (admin@pharmacy.com/Complexpass#123)
- **Prometheus**: http://localhost:9090

---

## 🔄 Mantenimiento

### Reiniciar Servicios de Monitoreo

```bash
cd /Users/nohelestradap/Documents/Proyecto_Arquitectura/Project_Arq/ensurancePharmacy

# Reiniciar todos los servicios de monitoreo
docker-compose -f docker-compose.prod.yml restart prometheus grafana openobserve

# O reiniciar individualmente
docker-compose -f docker-compose.prod.yml restart grafana
```

### Actualizar Configuración de Alertas

```bash
# Después de editar archivos de configuración
docker-compose -f docker-compose.prod.yml restart grafana

# Verificar que se cargaron los cambios
docker logs grafana-prod | grep alerting
```

### Backup de Configuración

```bash
# Backup de Grafana
docker exec grafana-prod tar czf /tmp/grafana-backup.tar.gz /var/lib/grafana
docker cp grafana-prod:/tmp/grafana-backup.tar.gz ./grafana-backup-$(date +%Y%m%d).tar.gz

# Backup de OpenObserve
docker exec openobserve-prod tar czf /tmp/openobserve-backup.tar.gz /data
docker cp openobserve-prod:/tmp/openobserve-backup.tar.gz ./openobserve-backup-$(date +%Y%m%d).tar.gz
```

---

## 📊 Ejemplo de Flujo de Alerta

```mermaid
graph LR
    A[Prometheus recolecta métricas] --> B{CPU > 70%?}
    B -->|No| A
    B -->|Sí| C[Esperar 5 minutos]
    C --> D{Aún CPU > 70%?}
    D -->|No| A
    D -->|Sí| E[Disparar Alerta]
    E --> F[Grafana procesa alerta]
    F --> G[Enviar Email]
    F --> H[Enviar Slack]
    G --> I[dnestrada@unis.edu.gt]
    G --> J[jflores@unis.edu.gt]
    H --> K[#unis-project]
```

---

## ✅ Resumen

Has configurado exitosamente un sistema completo de alertas con:

- ✅ **10 alertas automáticas** para sistema, aplicación y pipeline
- ✅ **Notificaciones por email** a 2 destinatarios
- ✅ **Notificaciones por Slack** al canal #unis-project
- ✅ **Dashboards visuales** con umbrales de alerta
- ✅ **Severidades configuradas** (Warning y Critical)
- ✅ **Políticas de repetición** para evitar spam

El sistema monitoreará automáticamente y enviará alertas cuando:
- 🔴 CPU o Memoria superen niveles críticos
- 🔴 El pipeline falle
- ⚠️ El performance de la aplicación se degrade
- ⚠️ Recursos del sistema estén bajos

**¡Tu sistema está protegido y monitoreado 24/7!** 🎉

