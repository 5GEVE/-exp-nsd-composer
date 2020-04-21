FROM maven:3.6.3-jdk-8 as mvn-build
# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Xmx4G"
WORKDIR /usr/src/mymaven
# Copy pom.xml and download all dependencies for offline use
COPY pom.xml pom.xml
RUN mvn -T 1C verify clean --fail-never
# Copy code
COPY ./src ./src
RUN mvn -T 1C verify

FROM azul/zulu-openjdk-alpine:8-jre
COPY --from=mvn-build /usr/src/mymaven/target/*.jar /app.jar
CMD java -jar app.jar
