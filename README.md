# ExamSpace - Online Examination Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.1.0-blue.svg)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-7.0.0-purple.svg)](https://vitejs.dev/)
[![MariaDB](https://img.shields.io/badge/MariaDB-Latest-blue.svg)](https://mariadb.org/)

## Project Status: In Progress

Welcome to **ExamSpace**! This is an online examination platform aimed at simplifying life for both teachers and students. Teachers can simply create, manage, and distribute exams with ease, and students have a hassle-free and stress-free exam experience. We're currently in the process of building and enhancing ExamSpace, so watch this space for more features!

## Features

### What You Can Do Right Now

- **User Accounts & Security**
- Securely sign up and log in with JWT authentication
  - Receive OTPs for signup and password recovery
  - Confirm your email address
  - Edit your profile (name, phone)
  - Safely reset your password

- **Exam Management**
  - Create, edit, and delete exams
  - Manually generate or generate using AI (Cohere API) questions
  - Upload files (PDF, DOCX, images) and extract content
- Extract text from images using OCR (Tesseract)
- CRUD questions easily
- Share exams with many users

- **Administering Exams**
  - Sign up for exams and begin when you're ready
  - Display a real-time timer and automatically submit when time's up
  - Monitor your progress and submission status

- **Document Processing**
  - Extract text from images and scanned documents using OCR
- Multi-file format support
  - Utilize extracted content to create questions

- **Contact & Support**
  - Contact through a contact form (emails directly reach admins)
  - Interact with the admin team directly

### Coming Soon
- Advanced reporting and analytics
- Question bank management
- Bulk operations for simplification
- Proctoring functionalities for safe exams
- Mobile application for accessing on-the-go

## Technology Stack

### Backend
- **Spring Boot 3.5.3** (Java 21)
- **MariaDB** for storing data
- **Spring Security** with JWT
- **Spring Data JPA** for ORM
- **JavaMail API** for email
- **Cohere API** for AI question generation
- **Tesseract OCR** for extracting text from images
- **Maven** for builds

### Frontend
- **React 19.1.0** with **Vite 7.0.0**
- **JavaScript (ES6+)** as scripting language
- **CSS3** for styling
- **Fetch API** for HTTP requests
- **React Router DOM** for routing

### DevOps & Tools
- **Git** for version control
- **VS Code** for development
- **JUnit** (backend) & **React Testing Library** (frontend) for testing
- **GitHub Actions** for CI/CD

---

**Note:** ExamSpace is in development. Features and APIs are subject to change as we scale. Thank you for your interest and support!