# Dockerfile para el frontend Vue.js
FROM node:18-alpine

# Instalar Python para el script getip.py
RUN apk add --no-cache python3

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración
COPY package*.json ./

# Instalar dependencias
RUN npm install

# Copiar el código fuente
COPY . .

# Variables de entorno para Docker
ENV VITE_API_URL=http://localhost:8081
ENV VITE_IP=localhost
ENV VITE_PORT=9008

# Crear el archivo .env para Docker
RUN echo "VITE_IP=localhost" > .env

# Exponer el puerto
EXPOSE 9008

# Comando para ejecutar la aplicación
CMD ["npm", "run", "dev"] 