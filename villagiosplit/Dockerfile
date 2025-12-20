FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw -q dependency:go-offline

COPY src src
RUN ./mvnw -q clean package -DskipTests

RUN cp target/*.jar app.jar

# Render usa a variável PORT
ENV PORT=8080
EXPOSE 8080

# Usar shell form para que a variável seja expandida corretamente
CMD java -Dserver.port=${PORT} -jar app.jar
