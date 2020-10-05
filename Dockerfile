FROM openjdk:8
COPY ./target/*.jar /app.jar
CMD java -jar app.jar
