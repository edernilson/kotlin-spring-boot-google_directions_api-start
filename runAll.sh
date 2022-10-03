#!/bin/bash

cd car-backend && ./mvnw clean package -DskipTests && cd ..

docker-compose down
docker-compose build
docker-compose up