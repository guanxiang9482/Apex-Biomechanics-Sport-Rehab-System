# Apex Biomechanics & Sports Rehab System
## CSE6234 Software Design — Group 3

## Prerequisites
- Java 21
- Maven 3.8+
- MySQL 8.0+

## Setup Instructions

### Step 1 — Database Setup
Log into MySQL as root and run:
```sql
CREATE DATABASE apex_db;
CREATE USER 'apex_user'@'localhost' IDENTIFIED BY 'Apex@2026';
GRANT ALL PRIVILEGES ON apex_db.* TO 'apex_user'@'localhost';
FLUSH PRIVILEGES;
```

### Step 2 — Import Schema and Seed Data
```bash
mysql -u apex_user -pApex@2026 apex_db < database/schema.sql
mysql -u apex_user -pApex@2026 apex_db < database/seed.sql
```

### Step 3 — Run Backend
```bash
mvn spring-boot:run
```
Backend runs on http://localhost:8080

### Step 4 — Run Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend runs on http://localhost:5173

## Demo Accounts
| Role | Username | Password |
|------|----------|----------|
| Admin | admin | Admin@2026 |
| Therapist | therapist1 | Admin@2026 |
| Athlete | Register via UI or admitted by Admin |

## Design Patterns Demo Path
1. Login as Admin → Admit New Athlete **(Facade Pattern)**
2. Login as Therapist → Log Biomechanical Data → Update Session Status **(Observer fires)**
3. Login as Admin → Process Billing → Select Strategy **(Strategy Pattern)**
4. Check Notifications **(Observer Pattern)**
5. Note single DBConnection instance throughout **(Singleton Pattern)**
