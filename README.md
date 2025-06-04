# PostPulse-Backend

**PostPulse-Backend** is a modern, scalable RESTful API backend for a blogging platform, built with Java and Spring Boot. It provides robust features such as user authentication, role-based authorization, post and category management, comment handling, and powerful search capabilities. This backend is designed to serve as a solid foundation for web and mobile blogging applications.

---

## Features

- **User Authentication & Authorization**
  - Secure JWT-based authentication.
  - Role-based access control (ADMIN, USER).

- **Post Management**
  - Full CRUD (Create, Read, Update, Delete) operations for blog posts.
  - Each post contains a title, description, content, and category association.

- **Category Management**
  - Organize posts into categories for easy navigation and filtering.

- **Comment System**
  - Users can add, view, and manage comments on blog posts.

- **Search Functionality**
  - Search for posts using keywords for better content discoverability.

- **API Documentation**
  - Interactive Swagger documentation for easy API exploration and testing.

---

## Technology Stack

- **Backend:** Java 11+, Spring Boot, Spring Security, Hibernate (JPA)
- **Database:** MySQL
- **API Testing:** Postman
- **Documentation:** Swagger
- **Build Tool:** Maven
- **Version Control:** Git

---

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven
- MySQL

### Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/PostPulse-Backend.git
   cd PostPulse-Backend
   ```

2. **Configure the Database**
   - Create a MySQL database (e.g., `postpulse_db`).
   - Update your database credentials in `src/main/resources/application.properties`:
     ```
     spring.datasource.url=jdbc:mysql://localhost:3306/postpulse_db
     spring.datasource.username=your_mysql_username
     spring.datasource.password=your_mysql_password
     ```

3. **Build and Run the Application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access the API Documentation**
   - Open your browser and navigate to: [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)

---

## Project Structure

```
PostPulse-Backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── pom.xml
└── README.md
```

---

## Usage

- Use Postman or any REST client to interact with the API endpoints.
- For authentication, obtain a JWT token using the login endpoint and include it in the `Authorization` header as `Bearer <token>`.

---

## Contributing

Contributions are welcome! Please fork the repository, create a new branch, and submit a pull request with your changes.

---

## License

This project is licensed under the MIT License.

---

## Acknowledgements

- Built with [Spring Boot](https://spring.io/projects/spring-boot)
- Inspired by modern blogging platform requirements
