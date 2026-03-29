# ─────────────────────────────────────────────
# Stage 1: Build & Run Tests
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS test-runner

LABEL maintainer="QA Team"
LABEL description="FakeStore API Test Suite - REST Assured + JUnit 5"

WORKDIR /app

# Copy dependency files first for Docker layer caching
COPY pom.xml .

# Download dependencies without running tests
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Run the test suite
# Reports will be in target/surefire-reports/
RUN mvn clean test -B

# ─────────────────────────────────────────────
# Stage 2: Export reports (optional lightweight stage)
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS reports

WORKDIR /reports
COPY --from=test-runner /app/target/surefire-reports ./surefire-reports

CMD ["sh", "-c", "echo 'Test reports available in /reports/surefire-reports/' && ls -la surefire-reports/"]
