FROM openjdk:17-jdk-slim
WORKDIR /app
ENV PORT 8080
EXPOSE 8080
COPY target/*.jar /app/app.jar
ENTRYPOINT exec java -Xmx128m $JAVA_OPTS -jar app.jar