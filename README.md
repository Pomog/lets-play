# We will create a Spring Boot CRUD API
This project implements a CRUD API using Spring Boot, Java, and MongoDB, and it is secured with JWT-based authentication.

The application follows a layered architecture (Controller, Service, Repository) and incorporates:
- Data validation
- Global exception handling
- CORS configuration
- Basic rate limiting

We adopt a step-by-step Test-Driven Development (TDD) approach using JUnit 5.

## 1. Project Setup with Spring Initializr
### 1.1  IntelliJ's "New Project > Spring Initializr"
![Spring Initializr wizard](img/1.png)
![Dependencies](img/2.png)

### 1.2 Add JWT, bean validation dependencies manually

### 1.3 Project Structure:
* com.example.letsplay (base package)
    * controller (REST controllers)
    * service (logic)
    * repository (for Spring Data MongoDB repositories)
    * model (for MongoDB data models)
    * config (any config)
    * exception (for custom exceptions)
    * dto (for data transfer objects)
### 1.3 Running MongoDB Using Vagrant
- We use a Vagrantfile to set up a CentOS-based virtual machine that runs Docker and a MongoDB container.
#### Accessing MongoDB
After starting the VM, MongoDB runs inside a Docker container with port 27017 forwarded to the VM. 
To access MongoDB from your host machine or another computer on the network, use the VM's IP address.
#### Verifying the Database
- SSH into your VM
```bash
vagrant ssh
sudo -i
docker exec -it mongodb bash
```
- Launch the MongoDB shell
```bash
mongosh
```
- Run
```bash
show dbs 
```
Find the VM's IP Address:
You can determine the IP address by running vagrant ssh and then using commands like ```ip addr```
![MongoDB](img/3.png)
### 1.4 Configure the MongoDB connection
```
src/main/resources/application.properties
```
```properties
spring.data.mongodb.uri=mongodb://<HOST>:27017/<DATABASE>
spring.data.mongodb.uri=mongodb://192.168.56.82:27017/lets-play
```
Replace <HOST> with the IP address or hostname of your MongoDB server and <DATABASE> with your database name

## Running the Application and Testing with Postman
```
vagrant up --provision
mvn spring-boot:run
```

- Register a new user:
```PostMan
{
  "name": "Test User",
  "email": "testuser@example.com",
  "password": "testpass"
}
```
- Login as ADMIN
```
POST http://localhost:8080/api/auth/login
  {
  "email": "admin@example.com",
  "password": "admin123"
  }
```

- Login with the new user
```PostMan
POST http://localhost:8080/api/auth/login
{
  "email": "testuser@example.com",
  "password": "testpass"
}
```

- Create a product with authentication
```powershell
To successfully create a product, we need an ADMIN user token. If you used the CommandLineRunner, an admin user admin@example.com with password admin123 exists. Use that: Login as admin: POST /api/auth/login with admin creds to get token.
Then use that token in Authorization header and POST product.
Expected: 201 Created, and returned JSON of product with an id. Now the product exists in DB.
```
```
POST http://localhost:8080/api/products
Headers:
Authorization  Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGVzIjpbIlJPTEVfQURNSU4iLCJST0xFX1VTRVIiXSwiaWF0IjoxNzQ0MjI1MTUyLCJleHAiOjE3NDQyMjg3NTJ9.-Xz1qApsb44CnCld9nFPkJKrC2Yu3TU902iJL6hj3AQ
{
    "name": "New Product",
    "description": "This is a great product.",
    "price": 19.99,
    "id": "67f6c4684f80a9283a42dbdd"
}
```
- Access public product list
```web
http://localhost:8080/api/products
```

- Get product by id
```powershell
http://localhost:8080/api/products/<id>
```