# ExamSpace - Online Examination Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.1.0-blue.svg)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-7.0.0-purple.svg)](https://vitejs.dev/)
[![MariaDB](https://img.shields.io/badge/MariaDB-Latest-blue.svg)](https://mariadb.org/)

## Project Status: In Progress

Welcome to **ExamSpace**! It is a web-based exam platform committed to simplifying the life of teachers and students. Teachers can effortlessly create, administer, and distribute exams, while students have a hassle-free and easy exam experience. We are currently at the development and growth stage of ExamSpace, so watch this space for more!

## Features

### What You Can Do Right Now

- **User Accounts & Security**
- Register securely and log in through JWT authentication
- Receive OTPs for registration and password recovery
- Confirm your email address
- Update your profile (name, phone)
- Revoke your password securely

- **Test Administration**
- Create, edit, and delete exams
- Hand-write or write using AI (Cohere API) questions
- Upload files (images, PDF, DOCX) and extract data
- Extract text from pictures using OCR with Tesseract
- Release tests to many users

- **Administering Tests**
- Register for exams and start when you're ready
- Monitor your progress and submission status

- **Document Processing**
- Convert text from images and scanned documents using OCR
- Multi-file format support
- Use the extracted information to create questions

## Technology Stack

### Backend
- **Spring Boot 3.5.3** (Java 21)
- **MariaDB** for data storage
- **Spring Security** with JWT
- **Spring Data JPA** for ORM
- **JavaMail API** for email
- **Cohere API** for question generation using AI
- **Tesseract OCR** for extracting text from images
- **Maven** for builds

### Frontend 
- **React 19.1.0** with **Vite 7.0.0**
- **JavaScript (ES6+)** as a scripting language
- **CSS3** for styling
- **Fetch API** for HTTP requests 
- **React Router DOM** for routing 

### DevOps & Tools 
- **Git** for version control 
- **VS Code** for coding 
- **JUnit** (backend) & **React Testing Library** (frontend) for testing 
- **GitHub Actions** for CI/CD 

--- **Note: ExamSpace is in development. Features and APIs may change as we expand. Appreciate your interest and support!**