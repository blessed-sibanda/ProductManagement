# Product Management System

Management of product info, reviews and recommendations

## SetUp Instructions

#### Clone this repo and build the project
```shell
./gradlew build
```

#### Build the docker images
```shell
docker-compose build
```

#### Run the system tests
```shell
chmod +x test-em-all.bash
./test-em-all.bash start stop
```

#### Run the microservices
```shell
docker compose up -d
```