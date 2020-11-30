FROM openjdk:8

ARG BUILD_DATE
ARG BUILD_VERSION

LABEL maintainer="matpergo@gmail.com"
LABEL org.label-schema.schema-version="1.0"
LABEL org.label-schema.build-date=$BUILD_DATE
LABEL org.label-schema.name="5GEVE/-exp-nsd-composer"
LABEL org.label-schema.description="A REST API module to compose experiment NSDs. It can also validate blueprints and generate NSDs from them."
LABEL org.label-schema.vcs-url="https://github.com/5GEVE/-exp-nsd-composer"
LABEL org.label-schema.version=$BUILD_VERSION
LABEL org.label-schema.docker.cmd="docker run -p 8086:8086 mpergolesi/exp-nsd-composer"

EXPOSE 8086
COPY ./target/*.jar /app.jar
CMD java -jar app.jar
