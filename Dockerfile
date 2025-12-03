FROM maven:3.8.5-openjdk-17 as builder
COPY pom.xml .
COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM amazoncorretto:17-alpine
RUN apk add --no-cache fontconfig ttf-dejavu
COPY --from=builder /target/CloudTechnologiesLabs-1.0.0-SNAPSHOT.jar cloud-labs.jar
CMD ["java", "-jar", "cloud-labs.jar"]