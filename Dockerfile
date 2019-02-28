FROM azul/zulu-openjdk-alpine:11.0.2 as dev
LABEL maintainer="Matteo Pergolesi"

LABEL org.label-schema.schema-version="1.0"

LABEL target=DEV

# Copy files
#COPY target/lib /usr/share/myservice/lib
RUN mkdir /context-composer
ARG JAR_FILE
ENV JAR_FILE=${JAR_FILE}
COPY target/${JAR_FILE} /context-composer
WORKDIR /context-composer

# Enable remote debugger
EXPOSE 5005
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

CMD java -cp ${JAR_FILE} it.cnit.nfvmano.App


FROM azul/zulu-openjdk-alpine:11.0.2 as prod

#ARG BUILD_DATE
#ARG REPOSITORY
#ARG DESCRIPTION
##ARG VCS_REF
#ARG VERSION
#ARG COMMAND
#LABEL org.label-schema.build-date=$BUILD_DATE
#LABEL org.label-schema.name=$REPOSITORY
#LABEL org.label-schema.description=$DESCRIPTION
##LABEL org.label-schema.vcs-ref=$VCS_REF
#LABEL org.label-schema.version=$VERSION
#LABEL org.label-schema.docker.cmd=$COMMAND
LABEL target=PROD

CMD java -cp ${JAR_FILE} it.cnit.nfvmano.App
