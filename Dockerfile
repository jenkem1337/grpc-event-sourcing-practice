FROM openjdk:23
WORKDIR /app
COPY ./out/artifacts/EventSourcing_jar/EventSourcing.jar /app/your-application.jar
ENTRYPOINT ["java", "-jar", "your-application.jar"]
