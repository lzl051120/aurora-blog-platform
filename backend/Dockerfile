FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q test && ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && apt-get clean && \
    useradd --system --uid 10001 aurora && mkdir -p /app/data/uploads && chown -R aurora:aurora /app
COPY --from=build /workspace/target/*.war /app/aurora-blog.war
USER aurora
EXPOSE 8081
HEALTHCHECK --interval=15s --timeout=5s --start-period=45s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8081/actuator/health/readiness || exit 1
ENTRYPOINT ["java", "-jar", "/app/aurora-blog.war"]
