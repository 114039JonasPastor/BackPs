# Etapa 1: Build con Maven
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar archivos de configuración
COPY pom.xml .

# Descargar dependencias (usar mvn directamente en lugar de mvnw)
RUN mvn dependency:go-offline -B

# Copiar código fuente y recursos
COPY src ./src

# Verificar que los archivos de migración existen
RUN ls -la src/main/resources/db/migration/ || echo "No migration files found"

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Verificar que el JAR incluye las migraciones
RUN jar -tf target/App-0.0.1-SNAPSHOT.jar | grep "db/migration" || echo "Migrations not in JAR"

# Etapa 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el JAR compilado
COPY --from=build /app/target/App-0.0.1-SNAPSHOT.jar app.jar

# Crear usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
