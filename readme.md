## Mint Account - Microservice

A Lesson in separating out account responsibilities from the monolithic mint service.
The patterns and practice was taken from [CRUD Microservice with AkkaHttp](https://medium.com/se-notes-by-alexey-novakov/crud-microservice-with-akkahttp-c914059bcf9f).

##### Functionality
- The ability to create an account given the account name doesn't already exist.

#### Prerequisites
- IDE - in this case I am using JetBrains IntelliJ IDEA
- Java JDK version - 1.8
- SBT
- Docker Desktop [Mac](https://docs.docker.com/docker-for-mac/install/), [Windows](https://docs.docker.com/docker-for-windows/install/) and [Linux](https://docs.docker.com/engine/install/)

### Running Tests
Tests are located in ```mint-account -> src -> test -> scala -> org.mint```
There are 4 kinds of tests
- Code Style Tests
These are used to make sure when writing that code styles are adhered to. 
- Unit Tests (Service Tests)
These use Mockito to mock the repository layer  and work by using ```when(repository method is called) return(value)```.
- Integration Tests (CommandRoute & QueryRoute Tests)
These tests use macwire for autowiring the service and repository layer.  
- End to End Tests
These test start up the application plus any dependencies stated in this case SQL Server instance (this requires Docker Desktop)
Each endpoint will be checked for an expected response based on a number of scenarios.

To run the tests you can either use the IDE or SBT
The IDE should have a right click option to run tests
SBT commands: 
- `sbt test:scalastyle` (runs Code Style)
- `sbt test` (runs Code Style, Unit & Integration Tests)
- ######TODO `sbt test:endtoend` (run End to end Tests)

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