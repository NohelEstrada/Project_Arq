# Dockerfile para el backend Java con SQLite
FROM openjdk:23-jdk-slim

# Instalar herramientas necesarias
RUN apt-get update && apt-get install -y \
    sqlite3 \
    maven \
    net-tools \
    && rm -rf /var/lib/apt/lists/*

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Descargar dependencias
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src
COPY init_sqlite_db.sql .
COPY README-SQLite.md .

# Crear base de datos SQLite si no existe e inicializarla
RUN sqlite3 pharmacy_db.sqlite < init_sqlite_db.sql || true

# Compilar la aplicación
RUN mvn clean compile -B

# Exponer el puerto
EXPOSE 8081

# Variables de entorno para SQLite
ENV DB_TYPE=sqlite
ENV JAVA_OPTS="-Ddb.type=sqlite"
ENV SERVER_PORT=8081

# Comando para ejecutar la aplicación
CMD ["mvn", "exec:java", "-Dexec.mainClass=com.sources.app.App", "-Ddb.type=sqlite"] 