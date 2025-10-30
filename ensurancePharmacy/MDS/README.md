# 📚 Documentación del Proyecto - Pharmacy System

Esta carpeta contiene toda la documentación markdown del proyecto organizada en un solo lugar.

## 📁 Contenido

### 🚨 Alertas y Monitoreo (Grafana)
- **ALERTAS-README.md** - Guía completa de configuración de alertas
- **ALERTAS-QUICK-START.md** - Inicio rápido con alertas
- **ALERTAS-CONFIGURACION-SMTP.md** - Configuración de email/SMTP
- **ALERTAS-RESUMEN-VISUAL.md** - Resumen visual de alertas
- **CONFIGURAR-SLACK.md** - Configuración de notificaciones Slack
- **MODIFICAR-ALERTAS.md** - Cómo modificar reglas de alertas
- **ARREGLAR-EMAIL.md** - Solución de problemas de email
- **SOLUCIONAR-NOTIFICACIONES.md** - Troubleshooting de notificaciones

### 📊 OpenObserve
- **README-OPENOBSERVE.md** - Documentación principal
- **QUICK-START.md** - Inicio rápido
- **PASO-A-PASO.md** - Guía paso a paso
- **RESUMEN-OPENOBSERVE.md** - Resumen general
- **CONFIGURAR-ALERTAS.md** - Configuración de alertas
- **COMO-CONFIGURAR-CONDICIONES.md** - Condiciones de alertas
- **STREAMS-DISPONIBLES.md** - Streams de métricas disponibles
- **IMPORTAR-ALERTAS.md** - Importar alertas a OpenObserve
- **IMPORTAR-DASHBOARDS.md** - Importar dashboards

### 🔄 Alert Forwarder
- **START-FORWARDER.md** - Servicio intermediario para alertas OpenObserve

### 🧪 Testing
- **RESUMEN-JMETER.md** - Tests de rendimiento con JMeter
- **RESUMEN-K6.md** - Tests de carga con K6

### 🔧 Configuración y Setup
- **SETUP-GUIDE.md** - Guía de configuración del sistema de monitoreo
- **RESUMEN.md** - Resumen del sistema de monitoreo
- **QUICK-FIX.md** - Soluciones rápidas a problemas comunes

### 🐳 Docker
- **README-Docker.md** - Documentación de Docker

### 📱 Aplicación
- **README-PUERTOS.md** - Configuración de puertos
- **README.md** - README principal

### 🔍 Comparativas
- **COMPARACION-HERRAMIENTAS.md** - Comparación de herramientas de testing

## 🗂️ Organización

Los documentos están agrupados por categorías:

```
MDS/
├── Alertas (Grafana)      - 8 documentos
├── OpenObserve            - 9 documentos
├── Alert Forwarder        - 1 documento
├── Testing                - 2 documentos
├── Configuración          - 3 documentos
├── Docker                 - 1 documento
├── Aplicación             - 2 documentos
└── Comparativas           - 1 documento
```

**Total**: 28 documentos markdown

## 🔍 Búsqueda Rápida

### ¿Cómo configurar alertas?
→ `ALERTAS-QUICK-START.md`

### ¿Problemas con notificaciones?
→ `SOLUCIONAR-NOTIFICACIONES.md`

### ¿Configurar OpenObserve?
→ `QUICK-START.md` o `README-OPENOBSERVE.md`

### ¿Tests de rendimiento?
→ `RESUMEN-JMETER.md` o `RESUMEN-K6.md`

### ¿Configuración de Slack?
→ `CONFIGURAR-SLACK.md`

### ¿Alert Forwarder?
→ `START-FORWARDER.md`

## 📝 Notas

- Los archivos se movieron desde `monitoring/`, `k6-tests/`, `jmeter-tests/`, etc.
- Los archivos .md de node_modules NO se incluyen (son de bibliotecas)
- Cada documento mantiene su nombre original para fácil referencia

## 🔗 Referencias

- Proyecto principal: `/Project_Arq/ensurancePharmacy/`
- Configuración de secretos: `/.env.prod` (NO subir a Git)
- Ejemplo de configuración: `/.env.example`

---

**Última actualización**: 30 de Octubre, 2025
