# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src/ src/

# The BuildKit cache keeps the Maven repository between builds (it is not part of the image)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q package -DskipTests -Djacoco.skip=true \
    && java -Djarmode=tools -jar target/oficina-mecanica-*.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S oficina && adduser -S oficina -G oficina

COPY --from=build --chown=oficina:oficina /workspace/extracted/dependencies/ ./
COPY --from=build --chown=oficina:oficina /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=oficina:oficina /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=oficina:oficina /workspace/extracted/application/ ./

USER oficina
EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
