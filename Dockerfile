FROM azul/zulu-openjdk-alpine:8-jre
COPY ./target/*.jar /app.jar
CMD java -jar app.jar
