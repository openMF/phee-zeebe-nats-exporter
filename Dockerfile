FROM openjdk:17-alpine AS builder

RUN apk update && apk add wget

COPY . ph-ee-exporter

WORKDIR /ph-ee-exporter

RUN ./gradlew --no-daemon -q clean bootJar

# =========================================
FROM openjdk:17-alpine as paymenthub

RUN mkdir -p /app/libs

COPY --from=builder /ph-ee-exporter/build/libs/*.jar /app/ph-ee-exporter.jar

WORKDIR /

EXPOSE 8200

CMD java -Dloader.path=/app/libs -jar /app/ph-ee-exporter.jar
