FROM openjdk:17-jdk-slim
COPY *.jar message-processor.jar
ENTRYPOINT ["java", "-jar", "/message-processor.jar"]