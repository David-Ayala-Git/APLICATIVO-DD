# syntax=docker/dockerfile:1
# =============================================================================
#  Backend unificado en UNA sola JVM (compatible con Render Free: 512 MB RAM)
# =============================================================================
# Compila dd_monolith (ms-common + los 7 microservicios de dominio + seguridad/CORS
# del gateway) en un unico jar ejecutable.
#   docker build -t aplicativo-dd .
#   docker run --env-file .env -p 10000:10000 aplicativo-dd
#
# Los Dockerfile de cada carpeta (dd_ms_*/Dockerfile) se mantienen intactos para
# docker-compose.yml cuando se despliegue en un servidor propio.

# ---- etapa 1: build ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /src

# Wrapper y build padre primero (capa estable)
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY ms-common ms-common
COPY dd_ms_gateway dd_ms_gateway
COPY dd_ms_auth dd_ms_auth
COPY dd_ms_crm dd_ms_crm
COPY dd_ms_billing dd_ms_billing
COPY dd_ms_network dd_ms_network
COPY dd_ms_support dd_ms_support
COPY dd_ms_comms dd_ms_comms
COPY dd_ms_reports dd_ms_reports
COPY dd_monolith dd_monolith

RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    sed -i 's/\r$//' gradlew && chmod +x gradlew \
    && ./gradlew bootJarMonolith --no-daemon

RUN mkdir /out \
    && cp "$(ls dd_monolith/build/libs/*.jar | grep -v -- '-plain.jar' | head -1)" /out/monolith.jar \
    && ls -lh /out/monolith.jar

# ---- etapa 2: runtime ----
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app app
WORKDIR /app
COPY --from=build /out/monolith.jar /app/monolith.jar
USER app

# Puerto que Render expone y techo de memoria ajustado al plan Free (512 MB)
ENV PORT=10000
ENV SPRING_PROFILES_ACTIVE=pdn,render
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -Xms64m -Xmx224m -XX:MaxMetaspaceSize=128m -XX:ReservedCodeCacheSize=48m -Xss256k -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError"
EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/monolith.jar"]
