\# Spring Boot User CRUD Application - Jenkins CI/CD Pipeline



\## Project Overview

This project is a \*\*Spring Boot CRUD Application\*\* that provides RESTful APIs for managing user data. It is built with \*\*Java 17\*\* and packaged using \*\*Maven 3\*\*. The application uses \*\*Spring Data JPA\*\* with an \*\*H2 in-memory database\*\* and exposes health endpoints through \*\*Spring Boot Actuator\*\*.



The project is integrated into a \*\*Jenkins CI/CD pipeline\*\*, which automates building, testing, quality analysis, security scanning, deployment, release, and monitoring.



---



\## Technologies Used

\- \*\*Java 17\*\*

\- \*\*Spring Boot 3.3.2\*\*

\- \*\*Spring Data JPA + H2 Database\*\*

\- \*\*Spring Boot Actuator\*\* (Monitoring \& Health Checks)

\- \*\*Maven 3\*\* (Build automation)

\- \*\*JUnit 5\*\* (Testing)

\- \*\*JaCoCo\*\* (Code coverage)

\- \*\*SonarCloud\*\* (Code quality analysis)

\- \*\*Trivy\*\* (Security scanning)

\- \*\*Docker\*\* (Containerisation \& deployment)

\- \*\*Jenkins\*\* (CI/CD automation)



---



\## Jenkins Pipeline Stages



\### 1. \*\*Build\*\*

\- Tool: Maven

\- Action: Compiles Java code and packages into a `.jar` file.

\- Command:  

&nbsp; ```bash

&nbsp; mvn -B -q -DskipTests package

&nbsp; ```



\### 2. \*\*Test\*\*

\- Tool: JUnit + Maven Surefire

\- Action: Executes unit and integration tests.

\- Jenkins publishes test results with the `junit` plugin.



\### 3. \*\*Code Quality\*\*

\- Tools: SonarCloud + JaCoCo

\- Action: Runs static code analysis \& collects coverage reports.

\- Command:  

&nbsp; ```bash

&nbsp; mvn clean verify sonar:sonar

&nbsp; ```



\### 4. \*\*Security\*\*

\- Tool: Trivy

\- Action: Scans project dependencies and Docker image for vulnerabilities.

\- Commands:  

&nbsp; ```bash

&nbsp; trivy fs .

&nbsp; trivy image my-app:latest

&nbsp; ```



\### 5. \*\*Deployment\*\*

\- Tool: Docker

\- Action: Builds Docker image and runs a local container for validation.

\- Commands:  

&nbsp; ```bash

&nbsp; docker build -t my-app:latest -f Dockerfile .

&nbsp; docker run -d --name my-app -p 8081:8080 my-app:latest

&nbsp; ```



\### 6. \*\*Release\*\*

\- Tool: Docker Hub (Registry)

\- Action: Pushes Docker image to Docker Hub for production release.

\- Steps:

&nbsp; - Authenticate using Jenkins credentials

&nbsp; - Push image with tag `latest`



\### 7. \*\*Monitoring\*\*

\- Tool: Spring Boot Actuator + Curl

\- Action: Monitors health status using the `/actuator/health` endpoint.

\- Command:  

&nbsp; ```bash

&nbsp; curl http://localhost:8081/actuator/health

&nbsp; ```



---



\## Repository

GitHub: \[My-app](https://github.com/melanidslv/My-app)



---



\## How to Run Locally



1\. Clone the repository:

&nbsp;  ```bash

&nbsp;  git clone https://github.com/melanidslv/My-app.git

&nbsp;  cd My-app

&nbsp;  ```



2\. Build and run the app:

&nbsp;  ```bash

&nbsp;  mvn spring-boot:run

&nbsp;  ```



3\. Access the application:

&nbsp;  - API: `http://localhost:8081/users`

&nbsp;  - Health: `http://localhost:8081/actuator/health`



4\. Run in Docker:

&nbsp;  ```bash

&nbsp;  docker build -t my-app:latest .

&nbsp;  docker run -d -p 8081:8080 my-app:latest

&nbsp;  ```



---



\## Author

\*\*Melani De Silva\*\*  

Master of Data Science(Professional) (SIT753)  



