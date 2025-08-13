# Configuración SQLite para ensurancePharmacy Backend v4

Este documento explica cómo configurar y ejecutar la aplicación con una base de datos SQLite local en lugar de Oracle.

## Cambios Realizados

### 1. Dependencias Agregadas (pom.xml)
- **sqlite-jdbc**: Driver JDBC para SQLite
- **hibernate-community-dialects**: Dialecto de Hibernate para SQLite

### 2. Archivos de Configuración
- **hibernate-sqlite.cfg.xml**: Configuración de Hibernate específica para SQLite
- **init_sqlite_db.sql**: Script de inicialización de la base de datos SQLite

### 3. Código Modificado
- **HibernateUtil.java**: Modificado para soportar cambio dinámico entre Oracle y SQLite

## Opciones de Ejecución

### Opción 1: Scripts Automatizados

#### Para macOS/Linux:
```bash
./run-with-sqlite.sh
```

#### Para Windows:
```cmd
run-with-sqlite.bat
```

### Opción 2: Comandos Manuales

#### 1. Compilar el proyecto:
```bash
mvn clean compile
```

#### 2. Inicializar la base de datos SQLite (solo la primera vez):
```bash
sqlite3 pharmacy_db.sqlite < init_sqlite_db.sql
```

#### 3. Ejecutar con SQLite:
```bash
# Usando variable de entorno
export DB_TYPE=sqlite
mvn exec:java -Dexec.mainClass="com.sources.app.Main"

# O usando propiedad del sistema
mvn exec:java -Dexec.mainClass="com.sources.app.Main" -Ddb.type=sqlite
```

### Opción 3: Desde IDE
1. Agregar la propiedad de sistema: `-Ddb.type=sqlite`
2. O establecer la variable de entorno: `DB_TYPE=sqlite`
3. Ejecutar la aplicación normalmente

## Estructura de la Base de Datos

### Archivo de Base de Datos
- **Ubicación**: `pharmacy_db.sqlite` (en el directorio raíz del proyecto)
- **Tipo**: Archivo SQLite local

### Esquema
- Las tablas serán creadas automáticamente por Hibernate (hbm2ddl.auto=update)
- El script `init_sqlite_db.sql` incluye configuraciones básicas y tablas adicionales

## Verificar la Configuración

### 1. Comprobar que se usa SQLite:
Al ejecutar la aplicación, deberías ver en la consola:
```
Using SQLite database configuration
```

### 2. Verificar la base de datos:
```bash
# Conectar a la base de datos SQLite
sqlite3 pharmacy_db.sqlite

# Listar tablas
.tables

# Ver esquema de una tabla
.schema USERS

# Salir
.quit
```

## Ventajas de SQLite para Desarrollo Local

1. **Sin instalación**: No requiere servidor de base de datos
2. **Portabilidad**: Un solo archivo contiene toda la base de datos
3. **Facilidad**: Ideal para desarrollo y pruebas locales
4. **Compatibilidad**: Hibernate gestiona las diferencias de SQL automáticamente

## Volver a Oracle

Para volver a usar Oracle, simplemente:
1. No establecer la variable `DB_TYPE` o establecerla a cualquier valor que no sea "sqlite"
2. O ejecutar sin la propiedad `-Ddb.type=sqlite`

La aplicación usará automáticamente la configuración de Oracle (`hibernate.cfg.xml`).

## Notas Importantes

1. **Entidades**: Todas las entidades definidas en `hibernate-sqlite.cfg.xml` serán mapeadas automáticamente
2. **Tipos de datos**: SQLite maneja automáticamente la conversión de tipos desde las anotaciones JPA
3. **Claves foráneas**: Están habilitadas con `PRAGMA foreign_keys = ON`
4. **Rendimiento**: SQLite es ideal para desarrollo, pero para producción se recomienda Oracle/PostgreSQL

## Troubleshooting

### Error de dialecto SQLite
Si encuentras errores relacionados con el dialecto:
```bash
mvn clean install
```

### Error de archivo no encontrado
Asegúrate de que el archivo `hibernate-sqlite.cfg.xml` esté en `src/main/resources/`

### Error de permisos (macOS/Linux)
```bash
chmod +x run-with-sqlite.sh
``` 