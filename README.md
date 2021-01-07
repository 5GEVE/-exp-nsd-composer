# 5G EVE Experiment NSD composer

A REST API module to compose experiment NSDs.
It can also validate blueprints and generate NSDs from them.

## Install

Docker images are available on [Docker Hub](https://hub.docker.com/r/mpergolesi/exp-nsd-composer).
Check the latest tag (version) available.
Run the application with:

```shell script
docker run -p 8086:8086 -d mpergolesi/exp-nsd-composer:<latest-tag>
```

Wait for the application to start, then test it with:

```shell script
curl http://localhost:8086/vsb/schema
```

## OpenApi

Once running, you can get the OpenAPI specification by visiting (change host if needed):

- http://localhost:8086/swagger-ui.html
- http://localhost:8086/api-docs

## Development

Some dependencies are not available in Maven repository. Check `pom.xml`.

Compile the project with:

```shell script
mvn clean package
```

Build the Docker image with:

```shell script
docker build --no-cache=true --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
    --build-arg BUILD_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) \
    -t <your-repo>/exp-nsd-composer:$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) .
```

## Graph export for visualization

Debug log often provides network topology export into graphviz format.
Running tests will produce output examples.

Copy the output to a text file called 'example.txt' and create a PNG with

```shell script
circo -Tpng example.txt -o example.png
```

Or copy the output to an online Graphviz editor like [Edotor](https://edotor.net/).

