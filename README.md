# Spring Boot Blog REST API

## Project Overview
The **Spring Boot Blog REST API** is a robust and scalable web service designed for a blogging platform. This application allows users to create, read, update, and delete blog posts while providing essential features such as user authentication, role-based authorization, category management, comment handling, and search functionality. Built with modern technologies, this API serves as a foundation for any frontend or mobile application.

## Key Features
- **User  Authentication & Authorization:** 
  - Implemented JWT (JSON Web Token) based authentication to secure endpoints.
  - Role management allows for different user permissions (e.g., ADMIN, USER).

- **CRUD Operations for Blog Posts:** 
  - Users can create, read, update, and delete blog posts.
  - Each post includes a title, description, content, and associated category.

- **Category Management:** 
  - Users can categorize posts, making it easier to organize and navigate content.

- **Comment Handling:** 
  - Users can add comments to posts, fostering interaction and engagement.

- **Search Functionality:** 
  - Users can search for posts based on keywords, enhancing discoverability.

- **API Documentation:** 
  - Comprehensive API documentation generated using Swagger, providing clear guidelines for developers.

## Technologies Used
- **Backend:** 
  - Java
  - Spring Boot
  - Spring Security
  - Hibernate (JPA)

- **Database:** 
  - MySQL

- **Tools:** 
  - Postman (for API testing)
  - Maven (for dependency management)
  - Swagger (for API documentation)

- **Version Control:** 
  - Git

## Getting Started

### Prerequisites
Before you begin, ensure you have the following installed:
- Java 11 or higher
- Maven
- MySQL

### Installation Steps
1. **Clone the Repository:**
   ```bash
   git clone https://github.com/yourusername/spring-boot-blog-rest-api.git
