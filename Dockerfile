# syntax=docker/dockerfile:1
# =============================================================================
#  Ecosistema completo en UNA imagen (Render detecta "Docker" por este archivo)
# =============================================================================
# Compila ms-common + los 8 microservicios con el build compuesto de la raiz
# (settings.gradle) y los corre juntos con entrypoint.sh.
#   docker build -t aplicativo-dd .
#   docker run --env-file .env -p 10000:10000 aplicativo-dd
#
# Los Dockerfile de cada carpeta (dd_ms_*/Dockerfile) se mantienen para
# docker-compose.yml, donde cada servicio va en su propio contenedor.

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

# Un solo paso de Gradle para todos (tests fuera: corren en local/CI con testAll)
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    sed -i 's/\r$//' gradlew && chmod +x gradlew \
    && ./gradlew bootJarAll --no-daemon

# Jar ejecutable de cada servicio con un nombre fijo: /out/<servicio>.jar
RUN mkdir /out && for s in gateway auth crm billing network support comms reports; do \
      cp "$(ls dd_ms_$s/build/libs/*.jar | grep -v -- '-plain.jar' | head -1)" "/out/$s.jar"; \
    done && ls -la /out

# ---- etapa 2: runtime ----
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app app
WORKDIR /app
COPY --from=build /out/ /app/
COPY entrypoint.sh /app/entrypoint.sh
RUN sed -i 's/\r$//' /app/entrypoint.sh && chmod +x /app/entrypoint.sh
USER app

# Puerto que Render expone (el gateway escucha aqui)
ENV PORT=10000
EXPOSE 10000
ENTRYPOINT ["/app/entrypoint.sh"]
