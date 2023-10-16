# HOWTO

### Requirements

* Docker / docker-compose

### Run


* docker-compose up

configuration directly on POD docker exec -it PODID /bin/bash

aws configure --profile default
--->
AWS Access Key ID [None]: test
AWS Secret Access Key [None]: test
Default region name [None]: eu-west-2
Default output format [None]:

aws s3 mb s3://my-bucket --endpoint-url http://localhost:4566

* run app
### Use SWAGGER UI

http://localhost:8080/swagger-ui/index.html
  
