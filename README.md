# Mock_OpenBanking

A Spring Boot application simulating an open banking credit evaluation API. It processes client credit requests based on FIN, age, salary, requested amount, and credit duration.

---

## Table of Contents

1. [Features](#features)
2. [Prerequisites](#prerequisites)
3. [Getting Started](#getting-started)

   * [Clone the Repository](#clone-the-repository)
   * [Build & Run](#build--run)
4. [API Endpoints](#api-endpoints)

   * [POST `/evaluate_client`](#post-evaluate_client)
5. [Credit Evaluation Rules](#credit-evaluation-rules)
6. [Testing](#testing)
7. [Usage Examples](#usage-examples)
8. [Configuration](#configuration)
9. [Contributing](#contributing)
10. [License](#license)

---

## Features

* Injects `FinRepository` to fetch client data.
* Validates age (18–65).
* Ensures requested amount ≤ 4× annual salary.
* Enforces credit duration between 6 months and 20 years.
* Returns JSON with `approved` status and descriptive messages.
* Includes comprehensive mock-based unit and integration tests.

---

## Prerequisites

* Java 11 or higher
* Maven 3.6+ (or Maven Wrapper)
* Git

---

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/kamaalg/Mock_OpenBanking.git
cd Mock_OpenBanking
```

### Build & Run

**Using Maven Wrapper**

```bash
./mvnw clean install  # compiles code and runs tests
./mvnw spring-boot:run
```

**Or package and run the JAR**

```bash
./mvnw clean package
java -jar target/mock-openbanking-0.0.1-SNAPSHOT.jar
```

The application listens on port `8080` by default.

---

## API Endpoints

### POST `/evaluate_client`

Evaluate a client’s credit request.

#### Request Body

```json
{
  "fin": "AZE12345678",
  "salary": 5000,
  "requested_amount": 20000,
  "age": 30,
  "credit_duration": "P365D"
}
```

| Field              | Type       | Description                               |
| ------------------ | ---------- | ----------------------------------------- |
| `fin`              | `String`   | Client’s FIN identifier                   |
| `salary`           | `Integer`  | Annual salary                             |
| `requested_amount` | `Integer`  | Desired credit amount                     |
| `age`              | `Integer`  | Client’s age                              |
| `credit_duration`  | `Duration` | ISO‑8601 duration (e.g. `P365D` = 1 year) |

#### Response Body

```json
{
  "approved": true,
  "message": "Congratulations! Credit approved with annual interest rate 7.50% for amount 20000."
}
```

| Field      | Type      | Description                                 |
| ---------- | --------- | ------------------------------------------- |
| `approved` | `boolean` | `true` if credit granted; `false` otherwise |
| `message`  | `String`  | Success details or reason for rejection     |

---

## Credit Evaluation Rules

1. **Age**: 18 ≤ age ≤ 65.
2. **Duration**: 6 months (≈ `P182D`) ≤ credit\_duration ≤ 20 years (≈ `P7300D`).
3. **Amount**: requested\_amount ≤ salary × 4.
4. **Interest Rate**: computed per business logic (tiered or formula-based).

Violations return `approved: false` with an explanatory message.

---

## Testing

This project includes unit and integration tests using Spring’s testing support and Mockito:

* **Unit Tests** (`src/test/java/...`):

  * Mock `FinRepository` to simulate database responses.
  * Validate controllers, services, and business logic in isolation.

* **Integration Tests** (`src/test/java/...`):

  * Use `@SpringBootTest` and `MockMvc` to exercise endpoints end-to-end.

Run tests with:

```bash
./mvnw test
```

---

## Usage Examples

**cURL Request**

```bash
curl -X POST http://localhost:8080/evaluate_client \
  -H "Content-Type: application/json" \
  -d '{
    "fin": "AZE12345678",
    "salary": 5000,
    "requested_amount": 20000,
    "age": 30,
    "credit_duration": "P365D"
  }'
```

**Sample Failure**

```json
{
  "approved": false,
  "message": "Credit duration must be between 6 months and 20 years."
}
```

---

## Configuration

Edit `src/main/resources/application.properties` to change settings:

```properties
server.port=8080
# Add other overrides here
```

---

## Contributing

1. Fork the repo
2. Create a feature branch (`git checkout -b feature/xyz`)
3. Commit your changes (`git commit -m "Add feature"`)
4. Push (`git push origin feature/xyz`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License. See `LICENSE` for details.
