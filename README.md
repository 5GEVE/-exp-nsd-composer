# 5geve-experiment-builder
A REST API module to compose VSB and CBs.
The project is designed to be deployed with Docker.
Check the pom.xml for details about plugins to ease docker image build.

## Developer guide

After cloning the repository you can create Docker images to debug the software.

To build a developer image with Maven, use the following (provides Java remote debugger).

```
mvn clean package docker:build -P dev
```

To build a developer image with Dockerfile, use the following (provides Java remote debugger).
JAR file must be compiled manually and passed as an argument.

```
docker build \
    --tag mpergolesi/5geve-experiment-builder:<version> \
    --target dev \
    --build-arg JAR_FILE=5geve-experiment-builder-<version>.jar 
```

To debug the app, create a running configuration in IDEA or Eclipse to connect your Java debugger
to `localhost:5005` .

To run the Docker container with Maven, use the following (this will automatically run the debugger
in IDEA):

```
mvn docker:run -P dev
```

To run the Docker container with plain Docker, use the following (substitute the image tag with your version):

```
docker run -it --rm -p 5005:5005 mpergolesi/5geve-experiment-builder:0.0.1-SNAPSHOT
```

### Graph export for visualization

`VsbGraph` and `NsdGraph` provide methods to export graphs to graphviz format.
Running tests will produce output examples.

Copy the output to a text file called 'example.txt' and create a PNG with
```
circo -Tpng example.txt -o example.png
```

Or copy the output to an online Graphviz editor like [Edotor](https://edotor.net/).

## User guide (release)

To build a production image with Maven, use the following (provides labels for the Docker image).

```
mvn clean package docker:build -P prod
```

To build a production image with Dockerfile, use the following (you can optionally setup labels as build-args).
JAR file must be compiled manually and passed as an argument.

```
docker build \
    --tag mpergolesi/5geve-experiment-builder:<version> \
    --target prod \
    --build-arg JAR_FILE=5geve-experiment-builder-<version>.jar \
    --build-arg BUILD_DATE=<date> \
    --build-arg REPOSITORY=mpergolesi \
    --build-arg DESCRIPTION="A REST API module to compose VSD and CDs." \
    --build-arg VERSION=<version> \
    --build-arg VCS_URL=<vcs-url> \
    --build-arg VCS_REF=<vcs-ref> \
    --build-arg COMMAND=<command> 
```
