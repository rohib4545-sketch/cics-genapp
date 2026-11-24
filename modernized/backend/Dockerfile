FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# repo.maven.apache.org throttles CI traffic; use the GCS mirror of Maven Central
RUN mkdir -p /root/.m2 && printf '<settings><mirrors><mirror><id>gcs-central</id><mirrorOf>central</mirrorOf><url>https://maven-central.storage-download.googleapis.com/maven2/</url></mirror></mirrors></settings>' > /root/.m2/settings.xml
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/genapp-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
