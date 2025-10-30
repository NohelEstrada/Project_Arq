# 🚨 Alertas - Guía Rápida

## 🚀 Inicio Rápido (5 minutos)

### 1️⃣ Configurar SMTP

Edita `monitoring/grafana/grafana.ini` y actualiza:
```ini
[smtp]
password = tu_contraseña_de_aplicacion_gmail
```

Y en `docker-compose.prod.yml`:
```yaml
- GF_SMTP_PASSWORD=tu_contraseña_de_aplicacion_gmail
```

### 2️⃣ Levantar Servicios

```bash
cd ensurancePharmacy
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```

### 3️⃣ Verificar en Grafana

1. Accede: http://localhost:3000 (admin/admin)
2. Ve a **Alerting** → **Alert rules**
3. Deberías ver 10 alertas

### 4️⃣ Configurar OpenObserve

```bash
cd monitoring/openobserve
./setup-alerts.sh
```

### 5️⃣ Probar

```bash
cd jmeter-tests
./run-stress-test.sh 100 120 8101
```

Espera 5 minutos y deberías recibir:
- ✉️ Email en dnestrada@unis.edu.gt y jflores@unis.edu.gt
- 💬 Mensaje en Slack #unis-project

---

## 📋 Alertas Configuradas

| Alerta | Condición | Duración | Severidad |
|--------|-----------|----------|-----------|
| CPU Alta | > 70% | 5 min | ⚠️ Warning |
| CPU Crítica | > 90% | 2 min | 🔴 Critical |
| Memoria Alta | > 80% | 5 min | ⚠️ Warning |
| Memoria Crítica | > 90% | 2 min | 🔴 Critical |
| Disco Alto | > 85% | 5 min | ⚠️ Warning |
| Response Time | > 1000ms | 3 min | ⚠️ Warning |
| Error Rate | > 5% | 3 min | ⚠️ Warning |
| Pipeline Fallido | Falla | 1 min | 🔴 Critical |
| Pipeline Lento | > 10min | 2 min | ⚠️ Warning |
| Queue Grande | > 5 jobs | 5 min | ⚠️ Warning |

---

## 🔧 Comandos Útiles

```bash
# Ver logs de Grafana
docker logs grafana-prod

# Reiniciar Grafana
docker-compose -f docker-compose.prod.yml restart grafana

# Probar webhook de Slack
curl -X POST $SLACK_WEBHOOK_URL \
  -H 'Content-Type: application/json' \
  -d '{"text": "Test de alerta"}'

# Ver estado de alertas
open http://localhost:3000/alerting/list
```

---

## ⚠️ Troubleshooting Rápido

### No llegan emails
1. Verifica contraseña de aplicación Gmail
2. Verifica 2FA habilitado en Gmail
3. Prueba desde Grafana: Contact Points → Email → Test

### No llegan mensajes Slack
1. Verifica webhook: `curl -X POST [webhook-url] -d '{"text":"test"}'`
2. Verifica canal #unis-project existe

### Alertas no se activan
1. Verifica Prometheus: http://localhost:9090/targets
2. Verifica métricas: http://localhost:9090/graph
3. Ejecuta stress test para forzar alerta

---

## 📞 Contactos

- **Emails**: dnestrada@unis.edu.gt, jflores@unis.edu.gt
- **Slack**: #unis-project
- **Grafana**: http://localhost:3000
- **OpenObserve**: http://localhost:5080

**Para más detalles, lee [ALERTAS-README.md](./ALERTAS-README.md)**

