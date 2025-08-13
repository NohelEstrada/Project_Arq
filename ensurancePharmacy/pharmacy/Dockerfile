# Use Node.js 18 as base image with Python support
FROM node:18-slim

# Install Python3 for the getip.py script
RUN apt-get update && \
    apt-get install -y python3 && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy package files first for dependency caching
COPY package*.json ./

# Install npm dependencies
RUN npm install

# Copy source code and configuration files
COPY . .

# Create a modified getip.py for Docker environment
RUN echo 'import socket\n\
\n\
def get_local_ip():\n\
    # In Docker, use the container IP or a default\n\
    try:\n\
        hostname = socket.gethostname()\n\
        ip = socket.gethostbyname(hostname)\n\
    except Exception:\n\
        ip = "0.0.0.0"\n\
    return ip\n\
\n\
if __name__ == "__main__":\n\
    ip = get_local_ip()\n\
    print("Local IP Address:", ip)\n\
    \n\
    # Write the IP to a .env file\n\
    with open(".env", "w") as env_file:\n\
        env_file.write(f"VUE_APP_API_HOST={ip}\\n")\n\
        env_file.write(f"VUE_APP_IP={ip}\\n")\n\
    print(".env file created successfully!")' > getip_docker.py

# Expose port 8080 (Vue.js default development server port)
EXPOSE 8080

# Run the application
CMD ["npm", "run", "serve"] 