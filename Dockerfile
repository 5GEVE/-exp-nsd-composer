FROM azul/zulu-openjdk-alpine:8-jre
# TODO check
COPY ./target/*.jar /app.jar
CMD java -jar app.jar
