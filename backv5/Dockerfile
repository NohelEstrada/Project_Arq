# Use OpenJDK 21 as base image
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src
COPY .env ./

# Create SQLite database file
RUN touch pharmacy_db.sqlite

# Build the application
RUN mvn clean compile -B

# Expose port 8081 (from .env file)
EXPOSE 8081

# Run the application
CMD ["mvn", "exec:java", "-Dexec.mainClass=com.sources.app.App"] 