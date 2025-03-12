# Kitchensink - Modern Approach

This project is a modernized approach of the [JBoss EAP Quickstarts Kitchensink](https://github.com/jboss-developer/jboss-eap-quickstarts/tree/8.0.x/kitchensink).

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
- **Application URL:** [http://localhost:5173](http://localhost:5173)
- **Swagger Documentation:** [http://localhost:80808/swagger-ui](http://localhost:8080/swagger-ui)

### Stopping the Project

To stop and remove all running containers, use:

```sh
docker compose down
```

## Additional Information

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