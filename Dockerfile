# --- Fase 1: Construcción (Build Stage) ---
# Usamos una imagen de Maven con Java 11 (openjdk-11)
FROM maven:3.8-openjdk-11 AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos primero el pom.xml para aprovechar el cache de capas de Docker
COPY pom.xml .

# Descargamos las dependencias
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente del proyecto
COPY src ./src

# Empaquetamos la aplicación en un archivo .war.
# El -DskipTests acelera la construcción en el pipeline de despliegue.
RUN mvn clean package -DskipTests


# --- Fase 2: Ejecución (Runtime Stage) ---
# Usamos una imagen oficial de Tomcat que también usa Java 11 para mantener la consistencia
FROM tomcat:10.1-jdk11-temurin

# Eliminamos la aplicación de bienvenida por defecto de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiamos el .war construido en la fase anterior al directorio webapps de Tomcat
# Lo renombramos a ROOT.war para que se despliegue en la raíz del servidor.
COPY --from=build /app/target/AgenteMensajesIA.war /usr/local/tomcat/webapps/ROOT.war

# El puerto estándar de Tomcat es 8080. Render detectará y usará esto.
EXPOSE 8080

# Comando por defecto para iniciar el servidor Tomcat
CMD ["catalina.sh", "run"]
