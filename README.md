# Apex Biomechanics Sport Rehab System

A full-stack enterprise application designed for sports rehabilitation tracking, role-based user management, and athletic data analysis. Built using a robust Java Spring Boot REST API backend and a responsive React frontend interface.

---

## 🛠️ System Prerequisites & Installation

Before deploying the application, download and install the required tools using the official links below:

### 1. Java Development Kit (JDK 21)
* **Download Link:** [Official Oracle JDK 21 Downloads](https://www.oracle.com/java/technologies/downloads/#java21)
* **Installation:** Run the downloaded installer (`.exe`) and follow the on-screen prompts to complete the installation.

### 2. Apache Maven (3.9+)
* **Download Link:** [Official Apache Maven Downloads](https://maven.apache.org/download.cgi)
* **File to choose:** Under **Files**, download the **Binary zip archive** (e.g., `apache-maven-3.9.x-bin.zip`).

### 3. MySQL Server & Workbench
* **Download Link:** [Official MySQL Installer for Windows](https://dev.mysql.com/downloads/installer/)
* **File to choose:** Download the **MySQL Installer MSI** web or full package. Use it to install both **MySQL Server 8.0+** and **MySQL Workbench**.

---

## ⚙️ Windows Environment Variables Setup (Maven)

To ensure the system recognizes the global `mvn` compiler execution keywords, follow these steps to link your downloaded Maven binary archive:

1. **Extract the Maven Zip:** Open the downloaded `apache-maven-3.9.x-bin.zip` and extract the folder inside directly into the root of your `C:\` Drive. The directory path must look exactly like this:
   ```text
   C:\apache-maven-3.9.x
2. Open System Settings: Press the Windows Key, type Environment Variables, and select Edit the system environment variables.
3. Edit System Path: Click the Environment Variables... button at the bottom right. In the bottom section titled System variables, locate and double-click the Path variable.
4. Add Maven Bin: Click New on the right side of the window, and paste the exact path to your Maven executable directory:
   ```text 
   C:\apache-maven-3.9.x\bin
5. Save & Apply: Click OK on all three open windows to save the changes.
6. Verify: Open a brand new terminal session (Command Prompt or PowerShell) and verify the configuration:
    ```text
    mvn -v
   
## Database Setup Instructions

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
-- 1. Execute the structural architecture definitions
SOURCE database/schema.sql;

-- 2. Execute the initial data seed records
SOURCE database/seed.sql;
```

## Backend Setup & Execution (Spring Boot)
1. Open your terminal and navigate directly to the project root folder (where pom.xml is located):
```bash
cd Apex-Biomechanics-Sport-Rehab-System
```
2. Open src/main/resources/application.properties and verify your local database access credentials:
```bash
spring.datasource.url=jdbc:mysql://localhost:3306/apex_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=apex_user
spring.datasource.password=Apex@2026
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

server.port=8080
```
3. Run Backend
```bash
mvn spring-boot:run
```
4. The backend server will process your dependencies and listen securely for API calls on port 8080.

## Frontend Setup & Execution (React)
1. Open a new, completely separate terminal window and navigate into the user interface directory:
```bash
cd frontend
```
2. Install the necessary package dependencies:
```bash
npm install
```
3. Boot up the local web development client:
```bash
npm run dev
```
4. Launch your browser and navigate to the local address displayed in your terminal console (typically http://localhost:5173) to interact with the system dashboard.

## Demo Accounts
| Role | Username | Password |
|------|----------|----------|
| Admin | admin | Admin@2026 |
| Therapist | therapist1 | Admin@2026 |
| Athlete | Register via UI or admitted by Admin |


## Project Details
Course: CSE 6234 Software Design
Term: 2610
Institution: Multimedia University (MMU)


## Design Patterns Demo Path
1. Login as Admin → Admit New Athlete **(Facade Pattern)**
2. Login as Therapist → Log Biomechanical Data → Update Session Status **(Observer fires)**
3. Login as Admin → Process Billing → Select Strategy **(Strategy Pattern)**
4. Check Notifications **(Observer Pattern)**
