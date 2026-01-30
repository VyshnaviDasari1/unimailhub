# UniMailHub

UniMailHub is a smart email management system inspired by Gmail, designed to improve email organization, security, and productivity by integrating job detection and alert mechanisms.

---

## 📌 Project Overview

UniMailHub provides a centralized platform where users can:
- Send and receive emails
- Automatically detect job-related emails
- Receive security and job alerts
- Manage inbox, sent, and priority emails efficiently

The system is built as a full-stack web application using modern backend and frontend technologies.

---

## 🚀 Features

- Secure user authentication (Login & Registration)
- Email compose, send, and receive functionality
- Automatic job email detection using keyword scanning
- Dedicated Jobs tab for job-related emails
- Alerts system for:
  - Login security notifications
  - Job and interview alerts
- Inbox, Sent Mail, and Alerts management
- Clean and user-friendly interface

---

## 🏗️ System Architecture

- **Frontend:** HTML, CSS, JavaScript
- **Backend:** Java Spring Boot (REST APIs)
- **Database:** MySQL
- **Architecture Style:** Layered architecture with service-based modules

---

## 🧩 Modules

- User Authentication Module
- Inbox Management Module
- Email Compose & Send Module
- Jobs Detection Module
- Alerts Management Module
- Database Management Module

---

## 🗄️ Database Design

The application uses a relational database structure with the following tables:
- Users
- Emails
- Jobs
- Alerts

Each table is designed to ensure data integrity and efficient access.

---

## 🔐 Login Flow

1. User enters email and password
2. Backend validates credentials
3. On success, user is redirected to inbox
4. On failure, an error message is displayed

---

## 📧 Email Send Flow

1. User composes an email
2. Clicks send
3. Backend processes the request
4. Email is saved in Sent Mail
5. Email is delivered to receiver inbox

---

## 🔔 Alerts Flow

- Alerts are triggered by login or job-related events
- Alerts are saved in the database
- Alerts are displayed in a dedicated Alerts tab

---

## 🛠️ How to Run the Project

1. Clone the repository
2. Open the project in an IDE (VS Code / IntelliJ)
3. Configure MySQL database credentials
4. Run the Spring Boot application
5. Open the application in a browser

---

## 🎯 Future Enhancements

- Advanced job classification using AI/ML
- Email analytics and insights
- Mobile application support
- Multi-language support
- Enhanced notification system

---

## 👩‍💻 Author

**Vyshnavi Dasari**  
MS in Computer Science  
