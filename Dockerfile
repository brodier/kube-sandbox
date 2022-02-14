#
# Build stage
#
FROM maven:3.8.4-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:11-jre-slim
COPY --from=build /home/app/target/tools-0.0.1-SNAPSHOT-jar-with-dependencies.jar /usr/local/lib/tools.jar
COPY --from=build /home/app/src/main/resources/application.conf /usr/local/lib/application.conf
EXPOSE 8080
ENTRYPOINT ["java", "-Dconfig.file=/usr/local/lib/application.conf", "-jar", "/usr/local/lib/tools.jar"]
