# Use an official Java 21 JDK image for building
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copy the Maven wrapper and pom.xml first to leverage caching
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Grant execute permissions to mvnw
RUN chmod +x mvnw

# Copy the rest of the project files
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# ---- Production Stage ----
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Set the timezone
ENV TZ=Asia/Kolkata
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Copy only the built JAR from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]