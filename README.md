# Cupid Matching Component

COIT13235 Enterprise Software Development — Assignment 1.

## Requirements covered

- `FR_Swipe`
- `FR_Swipe_History`
- `FR_Match`
- `FR_Swipe_More_Ethics`
- `FR_Web_UI`
- `NFR_100Users`
- `NFR_Persistence_Timeout`
- `NFR_Deployability`
- `NFR_Input_Sanitise`
- `NFR_Traceability`

## Technology

- Java 17
- Spring Boot
- Maven
- Spring Data JPA
- MySQL
- Thymeleaf
- JUnit

## Run locally

1. Create the local MySQL databases and user described below.

   Open MySQL Shell or the MySQL command-line client as an administrator and run:

   ```sql
   CREATE DATABASE IF NOT EXISTS cupid_matching;
   CREATE DATABASE IF NOT EXISTS cupid_matching_test;
   CREATE USER IF NOT EXISTS 'cupid'@'localhost' IDENTIFIED BY 'cupid';
   GRANT ALL PRIVILEGES ON cupid_matching.* TO 'cupid'@'localhost';
   GRANT ALL PRIVILEGES ON cupid_matching_test.* TO 'cupid'@'localhost';
   FLUSH PRIVILEGES;
   ```

   If MySQL binary logging is enabled, also run this as an administrator so
   the rollback integration test can create its temporary failure trigger:

   ```sql
   SET GLOBAL log_bin_trust_function_creators = 1;
   ```

   MySQL must be running locally on port `3306`. The application defaults to
   user `cupid` and password `cupid`; set `DB_USERNAME` and `DB_PASSWORD` to
   use different credentials.

2. Run the application:

   ```bash
   mvn spring-boot:run
   ```

3. Open:

   ```text
   http://localhost:8080
   ```

