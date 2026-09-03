# 🚀 Spring Boot 3 + Node.js Frontend + Docker Compose Starter Guide

Welcome! This repository serves as the baseline project and pre-class environment setup guide. Please follow the instructions below to configure your Windows + VS Code development environment, run the Spring Boot backend, containerize it with Docker, build a Node.js frontend proxy, and run the entire stack with Docker Compose.

---

## 📋 Student Onboarding Checklist

Complete all steps below before attending class:

- [x] Install **OpenJDK 21 (LTS)** & **Node.js (v20+)** on Windows
- [x] **VS Code Extensions Installed** (Java, Spring Boot, Lombok, Docker, npm)
- [x] Implement `HelloController` in `helloworld/` using `@RequiredArgsConstructor`
- [x] Test Spring Boot backend locally at `http://localhost:8080/calc?left=100&right=100`
- [x] Create `helloworld/Dockerfile` and run Spring Boot in Docker
- [x] Build Node.js Express frontend in `frontend/` with its `Dockerfile`
- [x] Create `docker-compose.yml` in the parent root directory and test the full stack at `http://localhost:3000`
- [x] Fork this repository, commit your setup, and submit a **Pull Request (PR)**

---

### 📱 Additional Setup (For Mobile Development / Future Modules)

> **Note:** The current starter project only covers backend and web frontend. You may pre-install them to work on Android/Mobile integration:

- [x] **Android Studio** (includes Android SDK & JDK)
- [x] **Android Emulator** or a physical **Android Device** (with USB Debugging enabled)

---

## 🛠️ Step 1: Environment Setup

### 1. Install OpenJDK 21, Node.js, and Docker (Windows)

- **OpenJDK 21:** Download and install **Eclipse Temurin JDK 21 (LTS)** or **Microsoft Build of OpenJDK 21**. Ensure **"Set JAVA_HOME"** and **"Add to PATH"** are selected during installation.
- **Node.js:** Download and install **Node.js LTS (v20+)** for Windows (includes `npm`).
- **Docker Desktop:** Download and install **Docker Desktop for Windows**. Ensure WSL 2 or Hyper-V backend is enabled and Docker Desktop is running.

### 2. Install VS Code Extensions

Open VS Code (`Ctrl + Shift + X`) and install the following extensions:

1. **Extension Pack for Java** (Microsoft)
2. **Spring Boot Extension Pack** (Microsoft)
3. **Lombok Annotation Support** (Microsoft / Gabriel BB)
4. **Docker** (Microsoft) – _Provides container logs, image management, and syntax highlighting_
5. **npm** or **npm Intellisense** (Christian Kohler) – _Provides script running and dependency auto-completion_

---

## ☕ Step 2: Spring Boot Backend Implementation & Local Test

### 1. File Location

Create `HelloController.java` inside `helloworld/src/main/java/com/bankdki/helloworld/HelloController.java`:

```java
package com.bankdki.helloworld;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HelloController {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @GetMapping("/")
    public String hello() {
        return "Hello World!";
    }

    @Value
    public static class Result {
        int left;
        int right;
        long answer;
    }

    @GetMapping("/calc")
    public Result calc(@RequestParam int left, @RequestParam int right) {
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("left", left)
                .addValue("right", right);

        return jdbcTemplate.queryForObject(
                "SELECT :left + :right AS answer",
                source,
                (rs, rowNum) -> new Result(left, right, rs.getLong("answer"))
        );
    }
}
```

### 2. Local Verification

1. Run application: Open `HelloworldApplication.java` in VS Code and click **Run**, or run `./mvnw spring-boot:run` inside `helloworld/`.
2. Test in browser:
   - Root: `http://localhost:8080/` -> Returns `"Hello World!"`
   - Calc: `http://localhost:8080/calc?left=100&right=100` -> Returns `{"left":100,"right":100,"answer":200}`

---

## 🐳 Step 3: Containerize Spring Boot with Docker

### 1. Create `helloworld/Dockerfile`

Create `Dockerfile` inside `helloworld/`:

```dockerfile
# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Create `helloworld/.dockerignore`

```text
.git
.gitignore
.vscode
.idea
target
*.log
```

### 3. Build & Run Standalone Docker Container

```cmd
# Navigate to helloworld directory
cd helloworld

# Build image
docker build -t helloworld:1.0 .

# Run container
docker run -d -p 8080:8080 --name helloworld-app helloworld:1.0

# Verify endpoint in browser: http://localhost:8080/calc?left=100&right=100

# Stop & remove container
docker stop helloworld-app
docker rm helloworld-app
```

---

## 🌐 Step 4: Create Node.js Frontend & Dockerfile

Create a sibling folder named `frontend/` next to `helloworld/`.

### 1. Create `frontend/package.json`

```json
{
  "name": "calc-frontend",
  "version": "1.0.0",
  "description": "Simple Node.js Express frontend proxy for Spring Boot backend",
  "main": "server.js",
  "scripts": {
    "start": "node server.js"
  },
  "dependencies": {
    "express": "^4.19.2",
    "http-proxy-middleware": "^3.0.0"
  }
}
```

### 2. Create `frontend/server.js`

```javascript
const express = require("express");
const { createProxyMiddleware } = require("http-proxy-middleware");
const path = require("path");

const app = express();
const PORT = process.env.PORT || 3000;
const BACKEND_URL = process.env.BACKEND_URL || "http://backend:8080";

app.use(express.static(path.join(__dirname, "public")));

app.use(
  "/api",
  createProxyMiddleware({
    target: BACKEND_URL,
    changeOrigin: true,
    pathRewrite: { "^/api": "" },
  }),
);

app.listen(PORT, () => {
  console.log(`Frontend running on http://localhost:${PORT}`);
});
```

### 3. Create `frontend/public/index.html`

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Spring Boot Calc Frontend</title>
    <style>
      body {
        font-family: Arial, sans-serif;
        margin: 40px;
        background-color: #f4f6f8;
      }
      .card {
        background: white;
        padding: 24px;
        border-radius: 8px;
        max-width: 400px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
      input {
        width: 80px;
        padding: 8px;
        margin: 4px;
        font-size: 16px;
      }
      button {
        padding: 8px 16px;
        font-size: 16px;
        background-color: #0066cc;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
      }
      .result {
        margin-top: 16px;
        font-size: 18px;
        font-weight: bold;
        color: #1e293b;
      }
    </style>
  </head>
  <body>
    <div class="card">
      <h2>Simple Calculator</h2>
      <div>
        <input type="number" id="left" value="100" /> +
        <input type="number" id="right" value="100" />
        <button onclick="calculate()">=</button>
      </div>
      <div class="result" id="result">Answer: 200</div>
    </div>

    <script>
      async function calculate() {
        const left = document.getElementById("left").value;
        const right = document.getElementById("right").value;
        try {
          const response = await fetch(`/api/calc?left=${left}&right=${right}`);
          const data = await response.json();
          document.getElementById("result").innerText =
            `Answer: ${data.answer}`;
        } catch (err) {
          document.getElementById("result").innerText = "Error calling backend";
        }
      }
    </script>
  </body>
</html>
```

### 4. Create `frontend/Dockerfile`

```dockerfile
FROM node:20-alpine
WORKDIR /app

COPY package*.json ./
RUN npm install --only=production

COPY . .

EXPOSE 3000
CMD ["npm", "start"]
```

---

## 🐙 Step 5: Multi-Container Setup with Docker Compose

Create `docker-compose.yml` in the **parent root folder** (containing both `helloworld/` and `frontend/`):

```yaml
version: "3.8"

services:
  backend:
    build:
      context: ./helloworld
      dockerfile: Dockerfile
    container_name: springboot-backend
    ports:
      - "8080:8080"
    networks:
      - app-network

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: node-frontend
    ports:
      - "3000:3000"
    environment:
      - BACKEND_URL=http://backend:8080
    depends_on:
      - backend
    networks:
      - app-network

networks:
  app-network:
    driver: bridge
```

### Execution Commands

Run in terminal from the parent root folder:

```cmd
# 1. Build and start both containers
docker compose up -d --build

# 2. Check container status
docker compose ps

# 3. Open browser and test UI
# URL: http://localhost:3000

# 4. Stop containers when done
docker compose down
```

---

## 📁 Repository Directory Structure

Your project directory should be look like :

```text
my-project/                         <-- Parent Root Directory
├── docker-compose.yml              (Multi-container orchestration)
├── helloworld/                     (Spring Boot Backend)
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── pom.xml
│   └── src/
└── frontend/                       (Node.js Frontend)
    ├── Dockerfile
    ├── package.json
    ├── server.js
    └── public/
        └── index.html
```

---

## 📤 Step 6: Submitting Your Work or Asking Questions

### Option A: Submitting Your Setup (Pull Request)

If you have completed the setup successfully:

1. Click **Fork** at the top right of this repository page to create a copy under your account.
2. Clone your fork locally, add your code, and push the changes back to your fork:
   ```cmd
   git add .
   git commit -m "feat: complete Spring Boot, Node.js frontend, and Docker Compose setup"
   git push origin main
   ```
3. Navigate to your fork on GitHub and click **New Pull Request** pointing back to this original repository.
4. Fill out the PR template with your **Full Name**, and check off all completed items.

---

### Option B: Need Help or Have Something to Check?

If you encounter errors during setup:

1. Open a **Draft Pull Request** or an **Issue** on your fork/this repository.
2. Describe the problem you are experiencing and paste the error trace from your console.
3. Check the **"Instructor Assistance Needed"** box in the PR template.
4. I will review your submitted code changes, leave inline feedback on your lines of code, and help you fix the configuration.

---

## 🔧 Step 7: Common Troubleshooting

| Error Message                                                                   | Root Cause                                  | Solution                                                                                                     |
| :------------------------------------------------------------------------------ | :------------------------------------------ | :----------------------------------------------------------------------------------------------------------- |
| `lombok.Generated cannot be resolved`                                           | Missing Lombok processor cache sync         | Run `Ctrl + Shift + P` -> **`Java: Clean Java Language Server Workspace`** -> Select **Restart and Delete**. |
| `For artifact {org.projectlombok:lombok:null:jar}: The version cannot be empty` | Missing `<parent>` block in `pom.xml`       | Ensure project inherits from `spring-boot-starter-parent`.                                                   |
| `Field injection is not recommended`                                            | `@Autowired` used directly on private field | Replace with `@RequiredArgsConstructor` on class and mark injected fields `private final`.                   |
