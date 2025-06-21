# --- Fase 1: Construcción (Build Stage) ---
# Usamos una imagen de Maven con Java 17, que es compatible con tus dependencias
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# --- Fase 2: Ejecución (Runtime Stage) ---
# Usamos una imagen de Tomcat que también usa Java 17
FROM tomcat:10.1-jdk17-temurin

RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/AgenteMensajesIA.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]