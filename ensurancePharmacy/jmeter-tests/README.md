# 🔥 Tests de Stress con Apache JMeter

## 📋 Descripción

Este directorio contiene los test plans de JMeter para realizar pruebas de stress en la aplicación de Farmacia.

## 🚀 Inicio Rápido

### 1. Asegúrate que la aplicación esté corriendo

```bash
cd ensurancePharmacy
docker-compose -f docker-compose.prod.yml up -d backend-prod
# O ejecuta el pipeline completo en Jenkins
```

### 2. Ejecutar el test de stress

```bash
cd jmeter-tests
./run-stress-test.sh
```

### 3. Ver resultados

El script generará un reporte HTML automáticamente:

```bash
open results/html-report-[timestamp]/index.html
```

## ⚙️ Configuración del Test

### Parámetros por Defecto

- **Usuarios concurrentes**: 50
- **Ramp-up time**: 30 segundos (aumenta gradualmente de 0 a 50 usuarios en 30s)
- **Loops por usuario**: 10 (cada usuario ejecuta el test 10 veces)
- **Puerto backend**: 8101 (producción)
- **Total de requests**: ~2000 (50 usuarios × 10 loops × 4 endpoints)

### Personalizar Parámetros

```bash
# Sintaxis:
./run-stress-test.sh [usuarios] [ramp-time] [puerto]

# Ejemplos:
./run-stress-test.sh 100 60 8101    # 100 usuarios, 60s ramp-up
./run-stress-test.sh 25 15 8084     # Test ligero para DEV
./run-stress-test.sh 200 120 8101   # Test pesado para producción
```

## 🎯 Endpoints Probados

El test de stress prueba los siguientes endpoints:

1. **GET /api2/medicines** - Listar todas las medicinas
2. **GET /api2/categories** - Listar todas las categorías
3. **GET /api2/prescriptions** - Listar todas las prescripciones
4. **GET /api2/medicines/1** - Obtener detalles de una medicina específica

Cada usuario ejecuta estos 4 endpoints en secuencia, con un "think time" aleatorio de 1±0.5 segundos entre requests.

## 📊 Resultados Generados

Después de ejecutar el test, encontrarás:

### 1. Archivo JTL (datos crudos)
```
results/test-results-[timestamp].jtl
```
Contiene todos los datos de cada request (timestamp, latencia, status, etc.)

### 2. Reporte HTML (visual)
```
results/html-report-[timestamp]/index.html
```
Contiene:
- **Dashboard**: Resumen general del test
- **Charts**: Gráficas de performance
- **Statistics**: Estadísticas detalladas por endpoint
- **Error rate**: Tasa de errores
- **Response times**: Tiempos de respuesta (min, max, avg, percentiles)

### 3. Resumen CSV
```
results/aggregate-report.csv
```
Datos agregados para análisis en Excel o herramientas similares

## 📈 Métricas Clave a Revisar

### En el Reporte HTML:

1. **Throughput** (rendimiento):
   - Cuántas requests por segundo puede manejar el servidor
   - **Esperado**: >100 req/s para APIs simples

2. **Response Time**:
   - **Average**: <500ms (bueno), <1000ms (aceptable)
   - **95th Percentile**: <1000ms (bueno), <2000ms (aceptable)
   - **Max**: No debería exceder 5000ms

3. **Error Rate**:
   - **Ideal**: 0%
   - **Aceptable**: <1%
   - **Problema**: >5%

4. **Latency**:
   - Tiempo de espera antes de recibir la primera respuesta
   - Debería ser bajo (<200ms)

## 🎨 Visualizar Resultados en Grafana

Los tests de stress generan carga en el backend, que se reflejará en Grafana:

1. Ejecuta el test de stress
2. Mientras corre, abre Grafana: http://localhost:3000
3. Ve a "Application Performance Dashboard"
4. Verás aumentar:
   - CPU Usage
   - Memory Usage
   - Network Throughput

## 🔧 Modificar el Test Plan

### Opción 1: Editar el archivo .jmx directamente

```bash
# Abrir con editor de texto
code pharmacy-stress-test.jmx
```

Busca y modifica:
- `<stringProp name="ThreadGroup.num_threads">`: Número de usuarios
- `<stringProp name="LoopController.loops">`: Loops por usuario
- `<stringProp name="HTTPSampler.path">`: Endpoints a probar

### Opción 2: Usar la GUI de JMeter

```bash
jmeter -t pharmacy-stress-test.jmx
```

Esto abrirá la interfaz gráfica de JMeter donde puedes:
- Agregar más endpoints
- Modificar timers
- Agregar validaciones
- Configurar listeners
- Guardar los cambios

## 📝 Escenarios de Test

### Test Ligero (Desarrollo)
```bash
./run-stress-test.sh 10 10 8084
```
- 10 usuarios
- 10 segundos de ramp-up
- Puerto DEV (8084)

### Test Medio (UAT)
```bash
./run-stress-test.sh 50 30 8091
```
- 50 usuarios
- 30 segundos de ramp-up
- Puerto UAT (8091)

### Test Pesado (Producción)
```bash
./run-stress-test.sh 100 60 8101
```
- 100 usuarios
- 60 segundos de ramp-up
- Puerto PROD (8101)

### Test Extremo (Límites)
```bash
./run-stress-test.sh 200 120 8101
```
- 200 usuarios simultáneos
- 2 minutos de ramp-up
- Para encontrar el límite del servidor

## 🎯 Objetivos de Performance

### Criterios de Aceptación

| Métrica | Objetivo | Límite Aceptable |
|---------|----------|------------------|
| Throughput | >50 req/s | >20 req/s |
| Avg Response Time | <500ms | <1000ms |
| 95th Percentile | <1000ms | <2000ms |
| Max Response Time | <3000ms | <5000ms |
| Error Rate | 0% | <1% |
| CPU Usage | <70% | <90% |
| Memory Usage | <70% | <90% |

## 🐛 Troubleshooting

### Error: "Connection refused"

**Problema**: El backend no está corriendo

**Solución**:
```bash
# Verificar que el backend esté activo
curl http://localhost:8101/api2/medicines

# Si no responde, levantar servicios
cd ensurancePharmacy
docker-compose -f docker-compose.prod.yml up -d
```

### Error: "Read timed out"

**Problema**: El servidor está sobrecargado

**Solución**:
- Reducir número de usuarios
- Aumentar ramp-up time
- Verificar recursos del servidor (CPU, memoria)

### Resultados no se generan

**Problema**: Permisos o directorio

**Solución**:
```bash
mkdir -p results
chmod 755 results
```

## 📚 Estructura de Archivos

```
jmeter-tests/
├── README.md                       # Este archivo
├── pharmacy-stress-test.jmx        # Test plan de JMeter
├── run-stress-test.sh              # Script de ejecución
└── results/                        # Resultados de los tests
    ├── test-results-[timestamp].jtl
    ├── html-report-[timestamp]/
    │   └── index.html
    └── aggregate-report.csv
```

## 🚀 Integración con Jenkins (Opcional)

Para ejecutar los stress tests desde Jenkins, agregar este stage al Jenkinsfile:

```groovy
stage('Stress Tests') {
    when {
        branch 'master'
    }
    steps {
        dir("${env.PROJECT_DIR}/jmeter-tests") {
            sh './run-stress-test.sh 50 30 8101'
        }
    }
    post {
        always {
            publishHTML([
                reportDir: "${env.PROJECT_DIR}/jmeter-tests/results/html-report-*",
                reportFiles: 'index.html',
                reportName: 'JMeter Stress Test Report'
            ])
        }
    }
}
```

## 💡 Tips

1. **Empezar pequeño**: Primero prueba con pocos usuarios (10-20) para verificar que todo funciona
2. **Aumentar gradualmente**: Incrementa usuarios hasta encontrar el límite
3. **Monitorear recursos**: Usa Grafana para ver CPU/Memory mientras corren los tests
4. **Ejecutar varios escenarios**: Light, Medium, Heavy, Extreme
5. **Documentar resultados**: Guarda los reportes HTML con timestamps

## 📞 Comandos Útiles

```bash
# Ver versión de JMeter
jmeter --version

# Abrir GUI de JMeter
jmeter

# Abrir test plan en GUI
jmeter -t pharmacy-stress-test.jmx

# Ejecutar test con logs detallados
jmeter -n -t pharmacy-stress-test.jmx -l results/test.jtl -j results/jmeter.log

# Generar reporte HTML desde JTL existente
jmeter -g results/test-results-[timestamp].jtl -o results/new-report/
```

## 📊 Analizar Resultados

### Dashboard del Reporte HTML

1. **APDEX (Application Performance Index)**
   - Score de satisfacción del usuario
   - >0.95: Excelente
   - 0.85-0.95: Bueno
   - <0.85: Pobre

2. **Requests Summary**
   - Total de requests
   - OK/KO count
   - Error rate

3. **Statistics**
   - Min/Max/Average response time
   - Standard deviation
   - 90th, 95th, 99th percentiles
   - Throughput

4. **Charts**
   - Response times over time
   - Active threads over time
   - Bytes throughput
   - Latencies

## ✅ Checklist para Demo

- [ ] Backend corriendo en puerto 8101
- [ ] JMeter instalado (`jmeter --version`)
- [ ] Test plan ejecutado (`./run-stress-test.sh`)
- [ ] Reporte HTML generado
- [ ] Grafana mostrando métricas durante el test
- [ ] Screenshots de resultados guardados

---

**Autor**: Configuración de stress tests para Proyecto Arquitectura
**Fecha**: Octubre 2025

