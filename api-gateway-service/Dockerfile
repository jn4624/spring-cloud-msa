FROM openjdk:17-ea-11-jdk-slim

VOLUME /tmp

COPY target/api-gateway-service-0.0.1-SNAPSHOT.jar api-gateway-service.jar

ENTRYPOINT ["java","-jar","api-gateway-service.jar"]
