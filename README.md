# 5g-eve-context-composer
A REST API module to compose VSD and CDs.

## Setup and run

The project is designed to be deployed with Docker.
Check the pom.xml for details about plugins to ease docker image build.

### Build image with Maven

Use the following for a dev image (with Java remote debugger).

```
mvn clean package docker:build -P prod
```

Use the following for a production ready iamge (with labels).

```
mvn clean package docker:build -P prod
```

### Build image with plain docker

Use the following for a dev image (with Java remote debugger).

```
docker build \
    --tag mpergolesi/context-composer:<version> \
    --target dev \
    --build-arg JAR_FILE=5g-context-composer-<version>.jar 
```


Use the following for a production ready iamge (apart from JAR_FILE, 
it is not neccessary to specify all build-args).

```
docker build \
    --tag mpergolesi/context-composer:<version> \
    --target prod \
    --build-arg JAR_FILE=5g-context-composer-<version>.jar \
    --build-arg BUILD_DATE=<date> \
    --build-arg REPOSITORY=mpergolesi \
    --build-arg DESCRIPTION="A REST API module to compose VSD and CDs." \
    --build-arg VERSION=<version> \
    --build-arg VCS_URL=<vcs-url> \
    --build-arg VCS_REF=<vcs-ref> \
    --build-arg COMMAND=<command> 
```
