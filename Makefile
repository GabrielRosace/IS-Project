build: 
	docker build -t nekoma .

run: 
	docker run -it --rm -v $$PWD:/app --name nekoma_container -p8080:8080 -p3000:3000 nekoma bash

exec:
	docker exec -it nekoma_container bash

