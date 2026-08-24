
# Trading Position Maintaining & Order Update System (Java 21)

Two decoupled, lightweight Spring Boot services that process order update streams from a CSV file and maintain in-memory net positions per symbol.

---

## 🏗️ Architecture & Communication

* **Inter-Service Mechanism:** Synchronous **HTTP REST (JSON)**.
  * **Rationale:** Requires zero external message brokers/dependencies, supports native HTTP status codes, and ensures verifiable end-to-end transport.
* **Payload Schema:**
  ```json
  {
    "event_id": "evt-0001",
    "symbol": "RELIANCE",
    "transaction_type": "BUY",
    "quantity": 90
  }
  ```
* **Concurrency & Safety:** `ConcurrentHashMap` and `ConcurrentHashMap.newKeySet()` are used in `PositionTrackerService`. This guarantees lock-free, atomic reads via `GET /position` while concurrent updates are being processed.
* **Streaming Memory Efficiency:** Reads CSV using `BufferedReader.readLine()` incrementally to avoid loading large files into memory.
* **Throttling:** Nanosecond delta pauses ensure emissions stay at $\le 50\text{ events/sec}$.

---

## 🚀 Step-by-Step Run Instructions

### 1. Build the System
```bash
mvn clean package
```

### 2. Run Position Maintaining Service
In Terminal 1:
```bash
java -jar position-service/target/position-service-1.0.0.jar
```
*(Runs on `http://localhost:8080`)*

### 3. Run Order Update Service
In Terminal 2:
```bash
java -jar order-service/target/order-service-1.0.0.jar
```
*(Processes `order_updates.csv` and streams to `position-service`)*

---

## ⚙️ Configuration

Override properties using Spring Boot environment variables or CLI arguments:

| Property | Default | CLI Flag Override Example |
| :--- | :--- | :--- |
| `order.csv-path` | `order_updates.csv` | `--order.csv-path=/path/to/orders.csv` |
| `order.target-url` | `http://localhost:8080`| `--order.target-url=http://127.0.0.1:9000` |
| `order.rate-limit` | `50` | `--order.rate-limit=25` |

---

## 📊 API Verification

Query positions at any time:
```bash
curl -X GET http://localhost:8080/position
```

**Response:**
```json
{
  "RELIANCE": 90,
  "TCS": -75,
  "INFY": 0,
  "SBIN": 200
}
```

---

## 🧪 Running Automated Tests

Execute all tests across both modules:
```bash
mvn test
```
