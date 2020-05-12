# 5G EVE Experiment NSD composer
A REST API module to compose the NSD of a VSB and multiple CBs.

## Install

Some dependencies are not available in Maven repository. Check `pom.xml`.

Compile the project with:

```
mvn clean package
```

We use Docker Compose for deployment. Run:

```
docker pull azul/zulu-openjdk-alpine:8-jre
docker-compose build
docker-compose up
```

## Graph export for visualization

Debug log often provides network topology export into graphviz format.
Running tests will produce output examples.

Copy the output to a text file called 'example.txt' and create a PNG with
```
circo -Tpng example.txt -o example.png
```

Or copy the output to an online Graphviz editor like [Edotor](https://edotor.net/).

