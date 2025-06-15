# Use a Gradle image with OpenJDK 17
FROM gradle:8.5-jdk17 AS builder

# Set working directory
WORKDIR /my-project

# Copy Gradle files first (to cache dependencies better)
COPY build.gradle settings.gradle gradlew gradle/ ./

# Download dependencies (layer caching)
# Copy Gradle files first (to cache dependencies better)
COPY build.gradle settings.gradle gradlew ./
COPY gradle/wrapper ./gradle/wrapper


# Debug: check if gradle-wrapper.jar exists here
RUN ls -l gradle/wrapper/

# Download dependencies (layer caching)
RUN ./gradlew dependencies --no-daemon


# Copy the rest of the source code
COPY . .

# Build the project and skip tests
RUN ./gradlew clean bootJar -x test --no-daemon

# Use a lightweight JDK base for the final image
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /my-project/build/libs/*.jar app.jar

# Expose port (optional)
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar", "--debug"]
