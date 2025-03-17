# Use OpenJDK as the base image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the build/libs directory to the container
COPY build/libs/*.jar /app/dotburgas.jar

# Expose the port (Update this if needed for each service)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "dotburgas.jar"]