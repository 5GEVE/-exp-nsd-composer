# 5G EVE Experiment NSD composer
A REST API module to compose experiment NSDs. It can also validate blueprints and generate NSDs from them. 

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

## OpenApi

Once running, you can get the OpenAPI specification by visiting (change host if needed):

- http://localhost:8086/swagger-ui.html
- http://localhost:8086/api-docs

## Graph export for visualization

Debug log often provides network topology export into graphviz format.
Running tests will produce output examples.

Copy the output to a text file called 'example.txt' and create a PNG with
```
circo -Tpng example.txt -o example.png
```

Or copy the output to an online Graphviz editor like [Edotor](https://edotor.net/).

