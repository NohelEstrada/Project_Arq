# 🚨 Resumen Visual del Sistema de Alertas

## 📊 Dashboard de Alertas

```
┌────────────────────────────────────────────────────────────────────────┐
│                    PHARMACY MONITORING SYSTEM                          │
│                   Sistema de Alertas Automáticas                       │
└────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  🖥️  ALERTAS DE SISTEMA                                                 │
├────────────┬──────────────┬──────────┬──────────┬─────────────────────┤
│ Métrica    │ Warning      │ Critical │ Duración │ Acción              │
├────────────┼──────────────┼──────────┼──────────┼─────────────────────┤
│ CPU        │ > 70%        │ > 90%    │ 5m / 2m  │ Investigar proceso  │
│ Memoria    │ > 80%        │ > 90%    │ 5m / 2m  │ Revisar memory leak │
│ Disco      │ > 85%        │ -        │ 5m       │ Limpiar espacio     │
└────────────┴──────────────┴──────────┴──────────┴─────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  🌐 ALERTAS DE APLICACIÓN                                               │
├────────────────┬──────────────┬──────────┬──────────┬──────────────────┤
│ Métrica        │ Threshold    │ Severity │ Duración │ Acción           │
├────────────────┼──────────────┼──────────┼──────────┼──────────────────┤
│ Response Time  │ > 1000ms     │ Warning  │ 3m       │ Revisar backend  │
│ Error Rate     │ > 5%         │ Warning  │ 3m       │ Revisar logs     │
└────────────────┴──────────────┴──────────┴──────────┴──────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  🔧 ALERTAS DE PIPELINE                                                 │
├────────────────┬──────────────┬──────────┬──────────┬──────────────────┤
│ Métrica        │ Threshold    │ Severity │ Duración │ Acción           │
├────────────────┼──────────────┼──────────┼──────────┼──────────────────┤
│ Build Failed   │ > 0 failures │ Critical │ 1m       │ Revisar Jenkins  │
│ Duration       │ > 10 min     │ Warning  │ 2m       │ Optimizar build  │
│ Queue Size     │ > 5 jobs     │ Warning  │ 5m       │ Agregar agents   │
└────────────────┴──────────────┴──────────┴──────────┴──────────────────┘
```

---

## 📧 Flujo de Notificaciones

```
┌──────────────┐
│  PROMETHEUS  │ ← Recolecta métricas cada 15s
└──────┬───────┘
       │
       ↓
┌──────────────┐
│   GRAFANA    │ ← Evalúa reglas de alertas cada 1m
│   ALERTING   │
└──────┬───────┘
       │
       ↓
┌──────────────────────────────────────────────┐
│  ¿Condición cumplida durante el tiempo?      │
│  Ej: CPU > 70% por más de 5 minutos          │
└──────┬───────────────────────────────────────┘
       │
       ├───────────────┬──────────────────────────┐
       ↓               ↓                          ↓
┌──────────────┐ ┌──────────────┐      ┌──────────────────┐
│    EMAIL     │ │    SLACK     │      │   OPENOBSERVE    │
│              │ │              │      │                  │
│ dnestrada@   │ │ #unis-project│      │  Dashboard +     │
│ unis.edu.gt  │ │              │      │  Alertas         │
│              │ │              │      │                  │
│ jflores@     │ └──────────────┘      └──────────────────┘
│ unis.edu.gt  │
└──────────────┘
```

---

## 🎯 Severidades de Alertas

### 🔴 CRITICAL (Crítica)
**Acción inmediata requerida**

```
┌─────────────────────────────────────────────┐
│ 🔴 CRITICAL ALERT                           │
├─────────────────────────────────────────────┤
│ • CPU > 90% por más de 2 minutos            │
│ • Memoria > 90% por más de 2 minutos        │
│ • Pipeline ha fallado                       │
├─────────────────────────────────────────────┤
│ Notificación:                               │
│ ├─ Inmediata (10 segundos)                  │
│ ├─ Email + Slack simultáneamente            │
│ └─ Repetir cada 30 minutos si persiste      │
└─────────────────────────────────────────────┘
```

### ⚠️ WARNING (Advertencia)
**Requiere atención pronto**

```
┌─────────────────────────────────────────────┐
│ ⚠️ WARNING ALERT                            │
├─────────────────────────────────────────────┤
│ • CPU > 70% por más de 5 minutos            │
│ • Memoria > 80% por más de 5 minutos        │
│ • Disco > 85%                               │
│ • Response Time > 1000ms                    │
│ • Error Rate > 5%                           │
│ • Pipeline > 10 minutos                     │
│ • Queue > 5 trabajos                        │
├─────────────────────────────────────────────┤
│ Notificación:                               │
│ ├─ Después de confirmar (30 segundos)       │
│ ├─ Email + Slack                            │
│ └─ Repetir cada 4 horas si persiste         │
└─────────────────────────────────────────────┘
```

---

## 📈 Ejemplo de Alerta en Tiempo Real

### 1. Estado Normal
```
┌──────────────────────────────────────┐
│ 🟢 Sistema Saludable                 │
├──────────────────────────────────────┤
│ CPU:        45%  ████████░░░  [OK]   │
│ Memoria:    60%  ████████░░░  [OK]   │
│ Disco:      70%  ████████░░░  [OK]   │
│ Response:   250ms            [OK]   │
│ Errors:     0.5%             [OK]   │
│ Pipeline:   ✅ Success              │
└──────────────────────────────────────┘
```

### 2. Alerta WARNING Activada
```
┌──────────────────────────────────────┐
│ ⚠️ Alerta de CPU Alta                │
├──────────────────────────────────────┤
│ CPU:        75%  ██████████░  [⚠️]   │
│ Memoria:    60%  ████████░░░  [OK]   │
│ Disco:      70%  ████████░░░  [OK]   │
│                                      │
│ ⏰ Tiempo en alerta: 5:23            │
│                                      │
│ 📧 Notificaciones enviadas:          │
│    ✅ dnestrada@unis.edu.gt          │
│    ✅ jflores@unis.edu.gt            │
│    ✅ Slack #unis-project            │
└──────────────────────────────────────┘
```

### 3. Alerta CRITICAL Activada
```
┌──────────────────────────────────────┐
│ 🔴 ALERTA CRÍTICA - CPU              │
├──────────────────────────────────────┤
│ CPU:        94%  ███████████  [🔴]   │
│ Memoria:    88%  ███████████  [⚠️]   │
│ Disco:      70%  ████████░░░  [OK]   │
│                                      │
│ ⏰ Tiempo en alerta: 2:45            │
│ 🚨 ACCIÓN INMEDIATA REQUERIDA        │
│                                      │
│ 📧 Notificaciones enviadas:          │
│    ✅ dnestrada@unis.edu.gt (x2)     │
│    ✅ jflores@unis.edu.gt (x2)       │
│    ✅ Slack #unis-project (x2)       │
│                                      │
│ Sugerencias:                         │
│ • Revisar procesos: top / htop      │
│ • Detener servicios no esenciales   │
│ • Escalar recursos si es necesario  │
└──────────────────────────────────────┘
```

---

## 📧 Ejemplo de Email de Alerta

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
De: Pharmacy Monitoring System
    <pharmacy.monitoring@gmail.com>
Para: dnestrada@unis.edu.gt, 
      jflores@unis.edu.gt
Asunto: 🚨 [CRITICAL] Critical CPU Usage Detected!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

⚠️ ALERTA CRÍTICA

El uso de CPU ha superado el 90% durante más de 2 
minutos.

Detalles:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Alerta:     CPU Usage Critical
Severidad:  CRITICAL 🔴
Métrica:    CPU Usage
Valor:      93.7%
Umbral:     90%
Duración:   2 minutos 15 segundos
Tiempo:     2025-10-22 14:35:00 UTC
Estado:     FIRING
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

⚠️ ACCIÓN INMEDIATA REQUERIDA

El uso de CPU del sistema está críticamente alto.
Investigue inmediatamente para prevenir degradación
del servicio.

Enlaces Útiles:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
• Dashboard: http://localhost:3000/d/app-perf
• Alertas: http://localhost:3000/alerting/list
• Métricas: http://localhost:9090/graph

Acciones Sugeridas:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
1. Revisar procesos con alto uso de CPU
2. Verificar logs de la aplicación
3. Considerar escalar recursos
4. Detener servicios no esenciales

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Pharmacy Monitoring System
Powered by Grafana Alerting
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

## 💬 Ejemplo de Mensaje en Slack

```
┌────────────────────────────────────────────────┐
│ Pharmacy System Alert BOT                      │
│ 2:35 PM                                        │
├────────────────────────────────────────────────┤
│                                                │
│ 🔴 CRITICAL CPU USAGE ALERT                    │
│                                                │
│ Severity: CRITICAL                             │
│ Metric: CPU Usage                              │
│ Current Value: 93.7%                           │
│ Threshold: 90%                                 │
│ Duration: 2 minutes                            │
│ Time: 2025-10-22 14:35:00                      │
│                                                │
│ ⚠️ IMMEDIATE ACTION REQUIRED                   │
│ The system CPU usage is critically high.       │
│ Investigate immediately to prevent service     │
│ degradation.                                   │
│                                                │
│ 🔗 View Dashboard                              │
│ 🔗 View Alerts                                 │
│                                                │
└────────────────────────────────────────────────┘
    👍 2    👀 1
```

---

## 🔄 Ciclo de Vida de una Alerta

```
1. NORMAL
   ↓
   [Métrica supera umbral]
   ↓
2. PENDING (esperando duración mínima)
   ↓ (si persiste)
   [Duración cumplida]
   ↓
3. FIRING (alerta activa)
   ↓
   [Enviar notificaciones]
   ↓
4. NOTIFICADO
   │
   ├─→ [Problema persiste] → Repetir notificación (cada 4h o 30m)
   │
   └─→ [Métrica vuelve a normal] → RESOLVED
       ↓
       [Enviar notificación de resolución]
       ↓
   5. NORMAL
```

---

## 📊 Estadísticas de Alertas

### Alertas Configuradas
- **Total**: 10 alertas
- **Críticas**: 3 (CPU, Memoria, Pipeline)
- **Advertencias**: 7 (Resto)

### Canales de Notificación
- **Email**: 2 destinatarios
- **Slack**: 1 canal (#unis-project)
- **Total notificaciones por alerta**: 3 mensajes

### Frecuencias
- **Evaluación**: Cada 1 minuto
- **Notificación Critical**: Cada 30 minutos
- **Notificación Warning**: Cada 4 horas

---

## 🎯 Matriz de Decisión

```
┌─────────────┬─────────┬──────────┬───────────┬─────────────┐
│ Métrica     │ Warning │ Critical │ Urgencia  │ Impacto     │
├─────────────┼─────────┼──────────┼───────────┼─────────────┤
│ CPU         │ 70%     │ 90%      │ Alta      │ Alto        │
│ Memoria     │ 80%     │ 90%      │ Alta      │ Alto        │
│ Disco       │ 85%     │ -        │ Media     │ Medio       │
│ Response    │ 1000ms  │ -        │ Media     │ Alto        │
│ Errors      │ 5%      │ -        │ Alta      │ Alto        │
│ Pipeline    │ -       │ Failed   │ Crítica   │ Alto        │
│ Duration    │ 10min   │ -        │ Baja      │ Medio       │
│ Queue       │ 5 jobs  │ -        │ Baja      │ Bajo        │
└─────────────┴─────────┴──────────┴───────────┴─────────────┘

Leyenda de Urgencia:
  🔴 Crítica:  Acción inmediata (< 5 min)
  🟠 Alta:     Acción pronta (< 30 min)
  🟡 Media:    Acción necesaria (< 2 horas)
  🟢 Baja:     Monitorear (< 1 día)
```

---

## 🏗️ Arquitectura del Sistema de Alertas

```
┌──────────────────────────────────────────────────────────────┐
│                        APLICACIÓN                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                   │
│  │ Backend  │  │ Frontend │  │ Jenkins  │                   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘                   │
└───────┼─────────────┼─────────────┼────────────────────────┘
        │             │             │
        ↓             ↓             ↓
┌──────────────────────────────────────────────────────────────┐
│                    RECOLECCIÓN DE MÉTRICAS                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ Node Exporter│  │  Prometheus  │  │ Spring       │       │
│  │  (Sistema)   │  │   Exporter   │  │ Actuator     │       │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘       │
└─────────┼──────────────────┼──────────────────┼──────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             ↓
                    ┌─────────────────┐
                    │   PROMETHEUS    │
                    │  (Almacenamiento│
                    │   y Agregación) │
                    └────────┬────────┘
                             │
                ┌────────────┼────────────┐
                │            │            │
                ↓            ↓            ↓
        ┌───────────┐  ┌───────────┐  ┌──────────────┐
        │  GRAFANA  │  │OPENOBSERVE│  │ ALERTMANAGER │
        │ (Alerting)│  │ (Alerting)│  │  (Opcional)  │
        └─────┬─────┘  └─────┬─────┘  └──────┬───────┘
              │              │                │
              └──────────────┼────────────────┘
                             ↓
                    ┌─────────────────┐
                    │ NOTIFICACIONES  │
                    ├─────────────────┤
                    │ • Email (SMTP)  │
                    │ • Slack Webhook │
                    │ • Dashboard UI  │
                    └─────────────────┘
                             ↓
                    ┌─────────────────┐
                    │  DESTINATARIOS  │
                    ├─────────────────┤
                    │ dnestrada@...   │
                    │ jflores@...     │
                    │ #unis-project   │
                    └─────────────────┘
```

---

## ✅ Resumen Final

### Lo que tienes configurado:

✅ **10 reglas de alertas** automáticas
✅ **2 severidades**: Critical y Warning
✅ **2 canales** de notificación: Email + Slack
✅ **2 plataformas** de alertas: Grafana + OpenObserve
✅ **3 destinatarios**: 2 emails + 1 canal Slack
✅ **Dashboards visuales** con umbrales de alerta
✅ **Políticas de repetición** inteligentes
✅ **Scripts de prueba** automatizados

### Próximos pasos:

1. ✅ Configurar SMTP con tu contraseña de aplicación Gmail
2. ✅ Levantar servicios con `docker-compose up -d`
3. ✅ Verificar alertas en Grafana
4. ✅ Ejecutar `./setup-alerts.sh` para OpenObserve
5. ✅ Probar con `./test-alerts.sh`

**¡Tu sistema está listo para monitoreo 24/7!** 🎉

