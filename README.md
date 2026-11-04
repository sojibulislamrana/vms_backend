# Vehicle Management System (VMS) - Backend

Spring Boot REST API backend providing secure, environment-driven SQL database connectivity, fleet vehicle lifecycle management, and centralized error handling.

---

## 🚀 REST API Endpoints

Base URL: `http://localhost:8080/api/v1/vehicles` (also mapped to `/api/vehicles`)

| Method | Endpoint | Description | Status Codes |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/vehicles` | Register a new vehicle | `201 Created`, `400 Bad Request`, `409 Conflict` |
| `GET` | `/api/v1/vehicles` | List all fleet vehicles | `200 OK` |
| `GET` | `/api/v1/vehicles/{id}` | Get vehicle by ID | `200 OK`, `404 Not Found` |
| `GET` | `/api/v1/vehicles/number/{number}` | Get vehicle by Vehicle Number | `200 OK`, `404 Not Found` |
| `PUT` | `/api/v1/vehicles/{id}` | Update existing vehicle | `200 OK`, `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| `DELETE` | `/api/v1/vehicles/{id}` | Decommission / delete vehicle | `204 No Content`, `404 Not Found` |
| `GET` | `/api/health` | Database connectivity health check | `200 OK`, `503 Service Unavailable` |

---

## 🚘 Vehicle Payload Specification

### Create / Update Request Body (`VehicleRequest`)
```json
{
  "vehicleNumber": "STK-1001",
  "company": "Stark Logistics",
  "type": "Electric",
  "brand": "Audi",
  "model": "e-tron GT",
  "status": "Active"
}
```

### Response Body (`VehicleResponse`)
```json
{
  "id": 1,
  "vehicleNumber": "STK-1001",
  "company": "Stark Logistics",
  "type": "Electric",
  "brand": "Audi",
  "model": "e-tron GT",
  "status": "Active",
  "createdAt": "2026-11-04T15:53:08.979",
  "updatedAt": "2026-11-04T15:53:08.979"
}
```

### Error Response Body (`ErrorResponse`)
```json
{
  "timestamp": "2026-11-04T09:53:22.977Z",
  "status": 409,
  "error": "Conflict",
  "message": "Vehicle with number 'STK-1001' already exists",
  "path": "/api/v1/vehicles",
  "validationErrors": null
}
```

When Bean Validation fails (`HTTP 400`), `validationErrors` details each invalid field:
```json
{
  "timestamp": "2026-11-04T09:53:32.632Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/v1/vehicles",
  "validationErrors": {
    "vehicleNumber": "Vehicle number must be between 2 and 50 characters",
    "company": "Company is required"
  }
}
```

---

## 🔒 Security & Credentials Policy

- **No Hardcoded Secrets**: Database connection strings, usernames, and passwords are read from environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER`).
- **Git Ignored Artifacts**: Local `.env` files, `.db` files, logs, and build artifacts are strictly filtered out by [`.gitignore`](.gitignore).
- A documented template is provided in [`.env.example`](.env.example).

---

## 💻 Running the Backend on Mac

### 1. Run with Default Local Database (Zero Setup)
```bash
cd vms_backend
./mvnw spring-boot:run
```

### 2. Run with External PostgreSQL Database
```bash
cd vms_backend
DB_URL="jdbc:postgresql://localhost:5432/vms_db" \
DB_USERNAME="postgres" \
DB_PASSWORD="your_password" \
DB_DRIVER="org.postgresql.Driver" \
./mvnw spring-boot:run
```

---

## 🧪 Testing

Run the full automated test suite (25 tests covering database connectivity, repository queries, business rules, and REST API controller integration):
```bash
cd vms_backend
./mvnw test
```

### Quick cURL Examples
```bash
# 1. Register a vehicle
curl -i -X POST http://localhost:8080/api/v1/vehicles \
  -H "Content-Type: application/json" \
  -d '{"vehicleNumber":"STK-1001","company":"Stark Logistics","type":"Electric","brand":"Audi","model":"e-tron GT","status":"Active"}'

# 2. List all vehicles
curl -i http://localhost:8080/api/v1/vehicles

# 3. Check health
curl -i http://localhost:8080/api/health
```
