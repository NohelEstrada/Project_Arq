#!/bin/bash

# Script para ejecutar prueba de estrés a Google.com con JMeter
# Autor: Generado automáticamente
# Descripción: Ejecuta 100 usuarios concurrentes haciendo peticiones a Google

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}==========================================${NC}"
echo -e "${GREEN}  Google Stress Test con JMeter${NC}"
echo -e "${GREEN}==========================================${NC}"
echo ""

# Verificar si JMeter está instalado
if ! command -v jmeter &> /dev/null; then
    echo -e "${RED}ERROR: JMeter no está instalado o no está en el PATH${NC}"
    echo -e "${YELLOW}Instala JMeter desde: https://jmeter.apache.org/download_jmeter.cgi${NC}"
    exit 1
fi

# Crear directorio de resultados si no existe
mkdir -p results

# Configuración de variables (puedes modificarlas)
USERS=${USERS:-100}        # Número de usuarios (threads)
RAMP_TIME=${RAMP_TIME:-10} # Tiempo de arranque en segundos
LOOPS=${LOOPS:-5}          # Número de iteraciones por usuario

echo -e "${YELLOW}Configuración de la prueba:${NC}"
echo "  - Usuarios (threads): $USERS"
echo "  - Tiempo de arranque (ramp-up): $RAMP_TIME segundos"
echo "  - Iteraciones por usuario: $LOOPS"
echo "  - URL objetivo: https://www.google.com"
echo ""
echo -e "${YELLOW}Total de peticiones: $(($USERS * $LOOPS))${NC}"
echo ""

# Preguntar confirmación
read -p "¿Deseas continuar con la prueba? (s/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[SsYy]$ ]]; then
    echo -e "${RED}Prueba cancelada.${NC}"
    exit 0
fi

echo ""
echo -e "${GREEN}Iniciando prueba de estrés...${NC}"
echo -e "${YELLOW}Presiona Ctrl+C para detener la prueba.${NC}"
echo ""

# Ejecutar JMeter en modo CLI (non-GUI)
jmeter -n \
    -t google-stress-test.jmx \
    -l results/google-stress-results.jtl \
    -j results/google-jmeter.log \
    -e -o results/google-html-report \
    -JUSERS=$USERS \
    -JRAMP_TIME=$RAMP_TIME \
    -JLOOPS=$LOOPS

# Verificar si la prueba se ejecutó correctamente
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}==========================================${NC}"
    echo -e "${GREEN}  Prueba completada exitosamente!${NC}"
    echo -e "${GREEN}==========================================${NC}"
    echo ""
    echo -e "${YELLOW}Resultados guardados en:${NC}"
    echo "  - Archivo JTL: results/google-stress-results.jtl"
    echo "  - Log de JMeter: results/google-jmeter.log"
    echo "  - Reporte HTML: results/google-html-report/index.html"
    echo ""
    echo -e "${YELLOW}Para ver el reporte HTML, ejecuta:${NC}"
    echo "  open results/google-html-report/index.html"
    echo ""
else
    echo ""
    echo -e "${RED}==========================================${NC}"
    echo -e "${RED}  Error al ejecutar la prueba${NC}"
    echo -e "${RED}==========================================${NC}"
    echo ""
    echo -e "${YELLOW}Revisa el log para más detalles:${NC}"
    echo "  cat results/google-jmeter.log"
    echo ""
    exit 1
fi


