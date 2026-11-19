# El gateway no depende de ms-common: su contexto de build es su propia carpeta.

# ---- etapa 1: build ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    sed -i 's/\r$//' gradlew && chmod +x gradlew \
    && ./gradlew bootJar --no-daemon -x test

# ---- etapa 2: runtime ----
FROM eclipse-temurin:21-jre
RUN useradd --system --no-create-home app
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
