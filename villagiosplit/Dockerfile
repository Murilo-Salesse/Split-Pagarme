FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw -q dependency:go-offline

COPY src src
RUN ./mvnw -q clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "/app/target/*.jar"]
