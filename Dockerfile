FROM openjdk:17-jdk-slim

ENV JAVA_OPTS="-Xmx128m -Xms64m"

COPY *.jar message-processor.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /message-processor.jar"]