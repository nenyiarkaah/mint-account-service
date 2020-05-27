## Mint Account - Microservice

A Lesson in separating out account responsibilities from the monolithic mint service.
The patterns and practice was taken from [CRUD Microservice with AkkaHttp](https://medium.com/se-notes-by-alexey-novakov/crud-microservice-with-akkahttp-c914059bcf9f).

##### Functionality
- The ability to create an account given the account name doesn't already exist.

### Run with Docker Compose

#### Requirements

- Docker daemon needs to be available for SBT packager plugin

#### Build Docker image

First you need to build an image. In order to do that just run SBT commands to build a service image:

```bash
sbt stage
sbt docker:publishLocal
```

#### Run Docker image

```bash
docker-compose -f composed/docker-compose.yml up -d
```

#### Stop Docker image

```bash
docker-compose -f composed/docker-compose.yml down -v
```

#### Assumptions

Currently this service connects to a Microsoft SQL Server. The assumption is that the server is already instantiated.

##TODO
Write docker script to spin up MS SQL container.