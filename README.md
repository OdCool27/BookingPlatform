# 🏡 BookingPlatform

A **Java-based Booking Platform** where **Guests** and **Property Owners** can log in and carry out their role-specific functions.  
- **Guests** can search and book available properties.  
- **Property Owners** can list and manage their properties.  

---

## ✨ Features

- 🔐 **Secure Authentication**
  - Passwords are hashed using **BCrypt** for strong security.
  - Password validation rules implemented for user safety.

- 🏠 **Guest & Owner Roles**
  - Guests can book properties.
  - Owners can list and manage their properties.

- 🆔 **Unique ID Generation**
  - Custom **IDGenerators** ensure unique identifiers for Users, Guests, Owners, and Bookings.

- 🗄️ **Database Integration**
  - JDBC connection to **PostgreSQL**.
  - DAO layer for database communication.
  - Centralized **DBUtil** class for managing connections.

- 📧 **Automated Emails**
  - Built with **Jakarta Mail API**.
  - Automated **Email Verification** on signup.
  - **Booking Confirmation** emails sent to guests.
  - Requires a Gmail account with an **App Password**.

---

## ⚙️ Technologies Used

- **Java** (Core, OOP, JDBC)
- **PostgreSQL** (Relational Database)
- **BCrypt** (Password Hashing)
- **Jakarta Mail API** (Email Automation)
- **Java Properties Files** (Configuration Management)

---

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/OdCool27/BookingPlatform.git
cd BookingPlatform
