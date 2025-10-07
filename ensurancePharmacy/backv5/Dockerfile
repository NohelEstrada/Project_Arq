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

# Copy .env file if it exists, otherwise create empty one
COPY .env* ./
RUN touch .env

# Create SQLite database file if it doesn't exist
RUN touch pharmacy_db.sqlite

# Compile the project
RUN mvn clean compile -B

# Expose port
EXPOSE 8081

# Set environment variables
ENV PHARMACY_PORT=8081
ENV PHARMACY_API_PREFIX=api2
ENV PHARMACY_ID=1

# Run the application
CMD ["mvn", "exec:java", "-Dexec.mainClass=com.sources.app.App"] 