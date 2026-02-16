#!/bin/bash
echo "Resetting local environment..."
docker-compose down -v
docker-compose up -d
./mvnw clean compile
echo "Reset complete!"
