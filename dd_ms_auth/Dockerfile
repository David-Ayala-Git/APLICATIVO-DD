# Contexto de build: la RAIZ del repositorio (ver docker-compose.yml), porque el
# servicio depende de ms-common y Gradle solo lo encuentra en mavenLocal.
#   docker build -f dd_ms_auth/Dockerfile .

# ---- etapa 1: build ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

# ms-common primero: se publica en el ~/.m2 del contenedor y esta capa queda en
# cache mientras no cambie su codigo.
COPY ms-common ms-common
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    cd ms-common && sed -i 's/\r$//' gradlew && chmod +x gradlew \
    && ./gradlew publishToMavenLocal --no-daemon -x test

COPY dd_ms_auth dd_ms_auth
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    cd dd_ms_auth && sed -i 's/\r$//' gradlew && chmod +x gradlew \
    && ./gradlew bootJar --no-daemon -x test

# ---- etapa 2: runtime ----
FROM eclipse-temurin:21-jre
RUN useradd --system --no-create-home app
WORKDIR /app
COPY --from=build /src/dd_ms_auth/build/libs/*.jar app.jar
USER app
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
