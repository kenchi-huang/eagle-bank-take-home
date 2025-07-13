Eagle Bank API

This project is a RESTful API for a modern banking application, built with Spring Boot. It provides a secure, robust, and well-tested backend for managing users, accounts, and financial transactions.

## Features

- **User Management:** Full CRUD (Create, Read, Update, Delete) operations for user profiles.
- **Account Management:** Full CRUD operations for bank accounts linked to users.
- **Transactional Fund Transfers:** Securely transfer funds between accounts with atomic operations to ensure data integrity.
- **Authentication & Authorization:** Modern JWT-based security to protect endpoints.
- **API Documentation:** Integrated Swagger UI for easy API exploration and testing.
- **Layered Architecture:** Clear separation of concerns between Controllers, Services, and Repositories.
- **Comprehensive Testing:** Extensive unit and integration test suite covering all layers of the application.

## Prerequisites

- Java 17 or later
- Apache Maven 3.6+
- An active internet connection (to download dependencies)

## Configuration

The application requires a secret key for generating and validating JWTs. This key must be configured in the `application.properties` file.

1.  Open `src/main/resources/application.properties`.
2.  Add the following property with a secure, Base64-encoded secret key. You can generate one from a site like base64encode.org.

## API Documentation (Swagger UI)
Once the application is running, you can access the interactive Swagger UI to explore and test all the API endpoints.
- Swagger UI: http://localhost:8080/swagger-ui/index.html

This interface provides a complete, up-to-date reference for all available endpoints, their required parameters, and example responses.

```
How to Build and Run

1.  **Clone the repository:**
```