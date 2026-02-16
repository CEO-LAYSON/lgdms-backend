.PHONY: build run test clean docker-up docker-down

build:
	./mvnw clean package

run:
	./mvnw spring-boot:run

test:
	./mvnw test

docker-up:
	docker-compose up -d

docker-down:
	docker-compose down

docker-build:
	docker-compose build

logs:
	docker-compose logs -f app

clean:
	./mvnw clean
	rm -rf target/
