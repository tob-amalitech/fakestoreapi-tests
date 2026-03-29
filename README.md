# FakeStore API Test Suite 🧪

REST Assured + JUnit 5 automated API test suite for [fakestoreapi.com](https://fakestoreapi.com), integrated with a Jenkins CI/CD pipeline.

---

## 📁 Project Structure

```
fakestoreapi-tests/
├── src/
│   └── test/
│       └── java/
│           └── tests/
│               ├── BaseTest.java        # Shared REST Assured config
│               ├── ProductsTest.java    # 10 product endpoint tests
│               ├── AuthTest.java        # 4 authentication tests
│               ├── CartsTest.java       # 7 cart endpoint tests
│               └── UsersTest.java       # 6 user endpoint tests
├── Dockerfile                           # Containerised test runner
├── Jenkinsfile                          # Declarative CI/CD pipeline
├── pom.xml                              # Maven dependencies & plugins
└── README.md
```

---

## 🧪 Test Coverage

| Test Class | Endpoints Covered | Tests |
|---|---|---|
| `ProductsTest` | GET, POST, PUT, PATCH, DELETE `/products` | 10 |
| `AuthTest` | POST `/auth/login` | 4 |
| `CartsTest` | GET, POST, PUT, DELETE `/carts` | 7 |
| `UsersTest` | GET, POST, PUT, DELETE `/users` | 6 |
| **Total** | | **27 tests** |

---

## 🛠️ Tech Stack

- **Java 17**
- **Maven 3.9+**
- **REST Assured 5.4.0** — HTTP client & response assertions
- **JUnit 5.10** — Test framework
- **Hamcrest 2.2** — Matchers for assertions
- **Maven Surefire Plugin** — Test runner & XML report generation
- **Jenkins** — CI/CD orchestration
- **Docker** — Containerised execution

---

## ▶️ Run Locally

### Prerequisites
- Java 17+
- Maven 3.9+

### Run all tests
```bash
mvn clean test
```

### Run a specific test class
```bash
mvn clean test -Dtest=ProductsTest
```

### Run a specific test method
```bash
mvn clean test -Dtest=ProductsTest#getAllProducts_shouldReturn200AndNonEmptyList
```

### View reports
After running, open:
```
target/surefire-reports/index.html
```

---

## 🐳 Run via Docker

### Build and run tests in one step
```bash
docker build -t fakestoreapi-tests .
```

Tests run automatically during the Docker build (in the `test-runner` stage).

### Run with a mounted reports volume
```bash
docker run --rm -v $(pwd)/reports:/app/target/surefire-reports fakestoreapi-tests
```

---

## 🔧 Jenkins Setup

### 1. Run Jenkins via Docker
```bash
docker run -d -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  --name jenkins \
  jenkins/jenkins:lts
```

Access Jenkins at: `http://localhost:8080`

### 2. Required Plugins
Install these from **Manage Jenkins → Plugins → Available**:
- Git
- Pipeline
- HTML Publisher
- JUnit
- Email Extension (Email-ext)
- Slack Notification (for Slack webhook support)
- Maven Integration
- Timestamper
- Build Timeout

### 3. Tool Configuration
Go to **Manage Jenkins → Tools**:
- Add JDK: name = `JDK17`, install automatically (OpenJDK 17)
- Add Maven: name = `Maven3`, install automatically (3.9.x)

### 4. Credentials Setup
Go to **Manage Jenkins → Credentials → Global → Add Credentials**. Add these as **Secret text**:

| ID | Value |
|---|---|
| `SLACK_WEBHOOK_URL` | Your Slack Incoming Webhook URL |
| `MAIL_USERNAME` | Your Gmail/SMTP username |
| `MAIL_PASSWORD` | Your Gmail App Password |
| `MAIL_RECIPIENT` | Email address to receive notifications |

### 5. Email/SMTP Configuration
Go to **Manage Jenkins → System → Extended E-mail Notification**:
- SMTP server: `smtp.gmail.com`
- Port: `465`
- Check "Use SSL"
- Username: your Gmail address
- Password: your Gmail App Password (generate at myaccount.google.com → Security → App Passwords)

### 6. Create the Pipeline Job
1. New Item → Pipeline → Enter a name
2. Under **Build Triggers**: Check "GitHub hook trigger for GITScm polling"
3. Under **Pipeline**:
   - Definition: `Pipeline script from SCM`
   - SCM: Git
   - Repository URL: `https://github.com/YOUR_USERNAME/fakestoreapi-tests.git`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
4. Save → **Build Now** to test manually first

---

## 🔗 GitHub Webhook Setup

> If Jenkins is running locally, first expose it with ngrok:
> ```bash
> ngrok http 8080
> # Copy the https://xxxx.ngrok.io URL
> ```

1. Go to your GitHub repo → **Settings → Webhooks → Add webhook**
2. **Payload URL**: `https://your-jenkins-url/github-webhook/`
3. **Content type**: `application/json`
4. **Trigger**: Just the push event
5. Click **Add webhook**

Push any commit — Jenkins should trigger automatically within seconds.

---

## 💬 Slack Notification Setup

1. Go to [api.slack.com/apps](https://api.slack.com/apps) → Create New App
2. Features → **Incoming Webhooks** → Activate
3. **Add New Webhook to Workspace** → Choose a channel
4. Copy the Webhook URL
5. Store it in Jenkins Credentials as `SLACK_WEBHOOK_URL`

---

## 📊 Pipeline Stages

```
Checkout → Build → Run Tests → Publish JUnit → Publish HTML → Archive
```

| Stage | What it will do |
|---|---|
| Checkout | Pulls latest code from GitHub |
| Build | Downloads Maven dependencies |
| Run Tests | Executes all 27 API tests |
| Publish JUnit | Publishes XML results to Jenkins |
| Publish HTML | Publishes Surefire HTML report |
| Archive | Archives report files as build artifacts |

Post-build notifications are sent via **Slack** and **Email** on every run.

---

## 📧 Notification Examples

**Email subject**: `[Jenkins] SUCCESS: FakeStore-API-Tests #12`

**Slack message** (on failure):
```
❌ Jenkins Build FAILURE
Job: FakeStore-API-Tests
Build #: 12
Branch: main
Duration: 45 sec
[View Build] [Test Report]
```
