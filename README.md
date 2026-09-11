# Cupid Enterprise System

COIT13235 Enterprise Software Development — Assignment 3.

## Requirements covered

### Profile lifecycle - Allaine

- `FR_Profile`
- `FR_Profile_Fetch`
- `FR_Profile_Keep_Ethics`

### Profile Picture - Tanzim 
-'FR_Profile_Picture'

### Matching - Jay

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

## Profile ethical safeguard

Account deletion is implemented as a soft deactivation so that the profile is
removed from discovery while existing swipe and match records remain valid.
The one global setting `cupid.profile.account-deletion-enabled` can disable
account deletion for every user. When it is enabled, the profile owner must
first open **Account settings** and select **Allow my account to be deleted**.
This does not delete the profile; it only enables the final deletion process.
The Profile component then requires a separate acknowledgement checkbox and
the exact word `DELETE` before the soft deletion is processed by the server.

