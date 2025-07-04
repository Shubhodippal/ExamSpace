# ExamSpace - Online Examination Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.1.0-blue.svg)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-7.0.0-purple.svg)](https://vitejs.dev/)
[![MariaDB](https://img.shields.io/badge/MariaDB-Latest-blue.svg)](https://mariadb.org/)

## 🚧 Project Status: In Progress

ExamSpace is a comprehensive online examination platform that allows educators to create, manage, and conduct exams while providing students with a seamless exam-taking experience. The project is currently under active development.

## ✨ Features

### Current Features
- **User Authentication & Authorization**
  - JWT-based authentication
  - OTP verification for signup and password reset
  - Email verification system
  - Profile management

- **Exam Management**
  - Create and manage exams
  - Question generation using AI (Cohere API)
  - Document upload and content extraction
  - Manual question addition
  - Question CRUD operations

- **Exam Taking System**
  - Exam registration and start functionality
  - Real-time timer management
  - Auto-submission on time expiry
  - Progress tracking

- **Document Processing**
  - OCR support with Tesseract integration
  - Multiple file format support (PDF, DOCX, images)
  - Content extraction for question generation

- **Contact System**
  - Contact form with email integration
  - Admin communication

### Planned Features
- Advanced analytics and reporting
- Question bank management
- Bulk operations
- Advanced proctoring features
- Mobile application

## 🛠 Technology Stack

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Database**: MariaDB
- **Security**: Spring Security with JWT
- **ORM**: Spring Data JPA
- **Email**: JavaMail API
- **AI Integration**: Cohere API
- **OCR**: Tesseract OCR
- **Build Tool**: Maven

### Frontend
- **Framework**: React 19.1.0
- **Build Tool**: Vite 7.0.0
- **Language**: JavaScript (ES6+)
- **Styling**: CSS3
- **HTTP Client**: Fetch API
- **Routing**: React Router DOM

### DevOps & Tools
- **Version Control**: Git
- **Development**: VS Code
- **Testing**: JUnit (Backend), React Testing Library (Frontend)


**Note**: This project is currently in development. Features and APIs may change as development progresses.