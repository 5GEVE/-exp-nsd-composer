FROM azul/zulu-openjdk-alpine:11.0.2 as copy

# Copy files
#COPY target/lib /usr/share/myservice/lib
RUN mkdir /context-composer
ARG JAR_FILE
ENV JAR_FILE=${JAR_FILE}
COPY target/${JAR_FILE} /context-composer
WORKDIR /context-composer


FROM copy as dev

LABEL target=DEV

# Enable remote debugger
EXPOSE 5005
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

CMD java -jar ${JAR_FILE}


FROM copy as prod

LABEL maintainer="Matteo Pergolesi"
LABEL org.label-schema.schema-version="1.0"
ARG BUILD_DATE
LABEL org.label-schema.build-date=$BUILD_DATE
ARG REPOSITORY
LABEL org.label-schema.name=$REPOSITORY
ARG DESCRIPTION
LABEL org.label-schema.description=$DESCRIPTION
ARG VERSION
LABEL org.label-schema.version=$VERSION
ARG VCS_URL
LABEL org.label-schema.vcs-url=$VCS_URL
ARG VCS_REF
LABEL org.label-schema.vcs-ref=$VCS_REF
ARG COMMAND
LABEL org.label-schema.docker.cmd=$COMMAND

CMD java -jar ${JAR_FILE}
