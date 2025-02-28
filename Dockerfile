FROM openjdk:23
WORKDIR /app
COPY ./out/artifacts/EventSourcing_jar/EventSourcing.jar /app/EventSourcing.jar
ENTRYPOINT ["java", "-jar", "EventSourcing.jar"]
