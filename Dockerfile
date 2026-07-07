FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN chmod +x mvnw && ./mvnw -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN addgroup --system bankcore && adduser --system --ingroup bankcore bankcore
COPY --from=build /workspace/target/bankcore-api-*.jar /app/bankcore-api.jar
USER bankcore
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/bankcore-api.jar"]
