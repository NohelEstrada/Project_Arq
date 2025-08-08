#!/bin/bash

# Script para ejecutar la aplicación con SQLite
echo "Configurando aplicación para usar SQLite..."

# Establecer la variable de entorno para usar SQLite
export DB_TYPE=sqlite

# Inicializar la base de datos SQLite si no existe
if [ ! -f "pharmacy_db.sqlite" ]; then
    echo "Inicializando base de datos SQLite..."
    sqlite3 pharmacy_db.sqlite < init_sqlite_db.sql
    echo "Base de datos SQLite inicializada."
fi

# Compilar y ejecutar la aplicación
echo "Compilando proyecto..."
mvn clean compile

echo "Ejecutando aplicación con SQLite..."
echo "La base de datos SQLite se encuentra en: $(pwd)/pharmacy_db.sqlite"

# Ejecutar la aplicación principal (ajustar la clase principal según sea necesario)
mvn exec:java -Dexec.mainClass="com.sources.app.App" -Ddb.type=sqlite 