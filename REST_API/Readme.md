# Overview

REST_API is an API that supports two methods: GET and POST. The request URL for both methods is http://localhost:8080/api/dogs/.

The POST request allows you to submit a JSON object representing a dog. It has the following format: 

{<br/>
  "name" : String,<br/>
  "breed" : String,<br/>
  "age" : int<br/>
}

This object is written in MongoDB. The GET request returns an array of all objects in the database.

# Architecture

REST_API is implemented through the Vert.x toolkit(https://vertx.io/docs/). It consists of two main modules: 

- HTTP server for routing the requests.
- Consumer module for writing the requests to the database and retrieving records from the database.

Both modules communicate through Vert.x event bus. A Vert.x event bus is a light-weight distributed messaging system which allows different parts of the application to communicate with each in a loosely coupled way.

# Installation and deployment

To install and run REST_API, follow these steps:

1. Download and install Docker from here: https://www.docker.com/.

2. Next, create a folder and put docker-compose.yml, Dockerfile and REST_API.jar in it.

Links: <br/>
https://github.com/chompalova/REST_API/blob/master/REST_API/docker-compose.yml
https://github.com/chompalova/REST_API/blob/master/REST_API/Dockerfile
https://github.com/chompalova/REST_API/blob/master/REST_API/REST_API.jar

3. Open a console and navigate to the folder you created in the previous step. 

4. To start MongoDB, type 

```
docker-compose up --build
``` 
5. Open another console and navigate to the folder you created in step 2.

6. Type

```
docker build -t falcon-vertx . 
```
and then 
```
docker run -it -p 8080:8080 --network host falcon-vertx
```

to run the Vert.x container.

7. Type 

```
docker ps
```
to list your containers.

8. Install curl from https://curl.haxx.se/. If you are on Windows, put the link to the .exe in the PATH environment variable.

9. To test the REST endpoint:

POST request:

$ curl -X POST http://localhost:8080/api/dogs -d "@./payload.json"<br/>
{<br/>
  "name" : "Ketty",<br/>
  "breed" : "Beagle",<br/>
  "age" : 12<br/>
}

GET request:

$curl http://localhost:8080/api/dogs/

You should get a JSON array of results.
