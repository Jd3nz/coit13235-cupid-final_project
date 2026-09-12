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

### Messaging - Hung

- `FR_Messages`
- `FR_Messages_History`
- `FR_Messages_Threads`
- `FR_Message_More_Ethics`
- `FR_Web_UI`

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

Account deletion is implemented as a soft deactivation: the profile is removed
from Cupid discovery while existing swipe and match records remain valid.

Cupid uses one global setting to control whether account deletion is available
for every user:

```properties
# src/main/resources/application.properties
cupid.profile.account-deletion-enabled=true
```

## Matching ethical safeguard

Cupid keeps swipe decisions under the user's control. The optional swipe
reminder is visible on the **Discover** page only when both controls below are
enabled:

- The application-wide setting in `application.properties`:

  ```properties
  cupid.matching.swipe-encouragement-enabled=true
  ```

  Set it to `false` to disable swipe reminders for every user with one setting.

- The selected profile's **Show me swipe reminders** checkbox in **Account
  Settings**. Clearing the checkbox and selecting **Save preferences** stores
  `false` in that user's `swipe_encouragement_enabled` value. When that profile
  returns to Discover, the reminder is not shown.

The swipe, pass, matching, and swipe-history features remain available whether
or not reminders are enabled.

## Messaging ethical safeguard

Cupid never requires a user to send a message. **Message notification** in
**Account Settings** is a saved, per-profile opt-in control for the optional
conversation prompt:

1. With **Show message notifications** selected, the chosen profile sees the
   optional reminder on the Messages page.
2. Clearing the checkbox and selecting **Save preferences** stores `false` in
   `message_coercion_enabled` for that profile.
3. The reminder is then hidden for that profile, while existing conversations,
   message history, and the ability to send messages remain available.

This behaviour demonstrates `FR_Message_More_Ethics`: feature can be disabled through a single saved setting.

