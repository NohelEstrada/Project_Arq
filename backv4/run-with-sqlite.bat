@echo off

REM Script para ejecutar la aplicación con SQLite en Windows
echo Configurando aplicación para usar SQLite...

REM Establecer la variable de entorno para usar SQLite
set DB_TYPE=sqlite

REM Inicializar la base de datos SQLite si no existe
if not exist "pharmacy_db.sqlite" (
    echo Inicializando base de datos SQLite...
    sqlite3 pharmacy_db.sqlite < init_sqlite_db.sql
    echo Base de datos SQLite inicializada.
)

REM Compilar y ejecutar la aplicación
echo Compilando proyecto...
mvn clean compile

echo Ejecutando aplicación con SQLite...
echo La base de datos SQLite se encuentra en: %cd%\pharmacy_db.sqlite

REM Ejecutar la aplicación principal (ajustar la clase principal según sea necesario)
mvn exec:java -Dexec.mainClass="com.sources.app.App" -Ddb.type=sqlite

pause 