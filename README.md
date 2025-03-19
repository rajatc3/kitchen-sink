# Kitchensink - The Dumping Platform

## Prerequisites

Ensure you have the following installed on your system:
- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

## Getting Started

### Starting the Project

Run the following command in the root directory of the project to start the application:

```sh
docker compose up -d
```

This will start all necessary services in detached mode.

### Accessing the Application

Once the services are up, you can access the application at:
- **Application URL:** [http://localhost](http://localhost)
- **Swagger Documentation:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### Stopping the Project

To stop and remove all running containers, use:

```sh
docker compose down
```

## Additional Information

Application Architecture : [Click here to view](https://viewer.diagrams.net/index.html?edit=_blank#Uhttps%3A%2F%2Fraw.githubusercontent.com%2Frajatc3%2Fkitchen-sink%2Frefs%2Fheads%2Fmaster%2FKitchensink%2520Architecture.drawio#%7B%22pageId%22%3A%22be88aK_8NiFmp7I-nFF2%22%7D)

For troubleshooting, logs can be viewed using:

```sh
docker compose logs -f
```

Ensure all dependencies are met before running the application. If you encounter any issues, check the container logs or verify that required ports are not in use.


## Resources

- [Help Guide](HELP.md)
- [Version Information](VERSIONS.md)

## MongoDB Connectivity
use the following command to connect to the mongodb instance once the docker image is running
```sh
mongosh "mongodb://root:secret@localhost:27017/kitchensink-db?authSource=admin"
```

Basic command to peek into the database
```sh
db.members.find().pretty()
```
