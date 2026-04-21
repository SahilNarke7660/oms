# Order Management System (OMS) — Microservices with Spring Boot

A production-grade microservices-based Order Management System built with Spring Boot, featuring service discovery, API gateway with JWT authentication, role-based authorization via Keycloak, and business rule validation using Drools.

---

## Architecture Overview

```
Client (Postman)
       │
       ▼
┌─────────────────┐
│   API Gateway   │  Port 8765
│  (Keycloak JWT  │  → validates token
│  + Role-based   │  → routes to services
│  Authorization) │
└────────┬────────┘
         │ routes via Eureka (lb://)
    ┌────┴────┐
    ▼         ▼
┌──────────┐  ┌──────────────┐
│  User    │  │    Order     │
│ Service  │  │   Service    │
│ Port 8081│  │  Port 8082   │
│  H2 DB   │  │  H2 DB       │
│          │  │  + Drools    │
└──────────┘  └──────┬───────┘
      ▲               │
      └───────────────┘
         Feign Client
      (validate user before
        placing order)

All services register with:
┌──────────────────┐
│  Eureka Server   │  Port 8761
│ Service Registry │
└──────────────────┘

Authentication:
┌──────────────────┐
│    Keycloak      │  Port 8180
│ Identity Provider│
└──────────────────┘
```

---

## Services

| Service | Port | Description |
|---|---|---|
| Eureka Server | 8761 | Service registry — all services register here |
| User Service | 8081 | Manages users, stores data in H2 DB |
| Order Service | 8082 | Places orders, validates user via Feign, runs Drools rules |
| API Gateway | 8765 | Single entry point, JWT validation, role-based routing |
| Keycloak | 8180 | Identity provider — issues and validates JWT tokens |

---

## Tech Stack

- **Spring Boot 3.3.5** — core framework
- **Spring Cloud Gateway** — API gateway with reactive routing
- **Netflix Eureka** — service discovery and registration
- **OpenFeign** — declarative HTTP client for service-to-service calls
- **Keycloak 25.x** — OAuth2/JWT authentication and authorization
- **Drools 8.x** — business rule engine for order validation
- **H2** — in-memory database for User and Order services
- **Spring Data JPA** — ORM for database operations
- **Maven** — build tool

---

## Roles and Access Control

| Role | User Service | Order Service |
|---|---|---|
| **ADMIN** | GET, POST, DELETE | GET, POST, DELETE |
| **USER** | GET only | GET, POST |
| **GUEST** | GET only | No access |

---

## Drools Business Rules (Order Service)

| Rule | Condition | Action |
|---|---|---|
| Quantity Limit | quantity > 10 | Reject — max 10 items per order |
| Laptop Stock Limit | product = Laptop AND quantity > 2 | Reject — max 2 Laptops per order |
| Blacklisted User | userId = 2 | Reject — user is blacklisted |

---

## Project Structure

```
oms/
├── eureka-server/
│   ├── src/main/java/com/example/eureka_server/
│   │   └── EurekaServerApplication.java
│   └── src/main/resources/application.properties
│
├── user-service/
│   ├── src/main/java/com/example/user_service/
│   │   ├── controller/UserController.java
│   │   ├── service/UserService.java
│   │   ├── repository/UserRepository.java
│   │   └── model/User.java
│   └── src/main/resources/application.properties
│
├── order-service/
│   ├── src/main/java/com/example/order_service/
│   │   ├── controller/OrderController.java
│   │   ├── service/OrderService.java
│   │   ├── service/OrderRuleService.java
│   │   ├── repository/OrderRepository.java
│   │   ├── model/Order.java
│   │   ├── model/OrderResponse.java
│   │   ├── model/UserDTO.java
│   │   ├── model/OrderValidationResult.java
│   │   ├── client/UserClient.java
│   │   └── config/
│   │       ├── DroolsConfig.java
│   │       └── FeignClientInterceptor.java
│   └── src/main/resources/
│       ├── application.properties
│       └── rules/order-rules.drl
│
└── api-gateway/
    ├── src/main/java/com/example/api_gateways/
    │   ├── ApiGatewaysApplication.java
    │   └── SecurityConfig.java
    └── src/main/resources/application.properties
```

---

## Keycloak Setup

### 1. Start Keycloak
```bash
cd keycloak-25.x.x/bin
kc.bat start-dev --http-port=8180   # Windows
./kc.sh start-dev --http-port=8180  # Mac/Linux
```

### 2. Create Realm
- Open `http://localhost:8180/admin`
- Create realm: `Sahil`

### 3. Create Client
- Client ID: `oms-client`
- Client authentication: **ON**
- Direct access grants: **ON**
- Leave all redirect URLs blank

### 4. Create Roles
Create these 3 roles in Realm Roles:
- `ADMIN`
- `USER`
- `GUEST`

### 5. Create Users

| Username | Password | Role |
|---|---|---|
| admin1 | admin123 | ADMIN |
| user1 | user123 | USER |
| guest1 | guest123 | GUEST |

---

## Startup Order

Always start in this order:

```
1. Keycloak     → http://localhost:8180
2. Eureka       → http://localhost:8761
3. User Service → http://localhost:8081
4. Order Service→ http://localhost:8082
5. API Gateway  → http://localhost:8765
```

Wait ~15 seconds after all start. Verify all services are registered at `http://localhost:8761`.

---

## Getting a Token (Postman)

```
POST http://localhost:8180/realms/Sahil/protocol/openid-connect/token
Body: x-www-form-urlencoded
```

| Key | Value |
|---|---|
| client_id | oms-client |
| client_secret | (from Keycloak → Clients → oms-client → Credentials) |
| grant_type | password |
| username | admin1 |
| password | admin123 |

Copy the `access_token` from the response and use it as:
```
Authorization: Bearer <token>
```

---

## API Endpoints

All requests go through the Gateway on port **8765**.

### User Service

| Method | URL | Access | Description |
|---|---|---|---|
| GET | `/user-service/users` | ADMIN, USER, GUEST | Get all users |
| GET | `/user-service/users/{id}` | ADMIN, USER, GUEST | Get user by ID |
| POST | `/user-service/users` | ADMIN only | Create user |
| DELETE | `/user-service/users/{id}` | ADMIN only | Delete user |

**POST Body:**
```json
{
    "name": "Test User",
    "email": "test@example.com"
}
```

### Order Service

| Method | URL | Access | Description |
|---|---|---|---|
| GET | `/order-service/orders` | ADMIN, USER | Get all orders |
| GET | `/order-service/orders/{id}` | ADMIN, USER | Get order by ID |
| POST | `/order-service/orders` | ADMIN, USER | Place order |
| DELETE | `/order-service/orders/{id}` | ADMIN only | Delete order |

**POST Body:**
```json
{
    "userId": 1,
    "product": "Laptop",
    "quantity": 2
}
```

---

## Drools Rule Testing

**Test 1 — Quantity > 10 (rejected):**
```json
{ "userId": 1, "product": "Mouse", "quantity": 11 }
```
Response: `400 Bad Request`
```json
{ "message": "Order rejected: quantity cannot exceed 10 items." }
```

**Test 2 — Laptop quantity > 2 (rejected):**
```json
{ "userId": 1, "product": "Laptop", "quantity": 3 }
```
Response: `400 Bad Request`
```json
{ "message": "Order rejected: maximum 2 Laptops allowed per order." }
```

**Test 3 — Blacklisted user (rejected):**
```json
{ "userId": 2, "product": "Phone", "quantity": 1 }
```
Response: `400 Bad Request`
```json
{ "message": "Order rejected: user is blacklisted." }
```

**Test 4 — Valid order (accepted):**
```json
{ "userId": 1, "product": "Laptop", "quantity": 2 }
```
Response: `201 Created`

---

## Key Concepts Implemented

| Concept | Where |
|---|---|
| Service Discovery | Eureka Server + `@EnableDiscoveryClient` on all services |
| API Gateway | Spring Cloud Gateway with `lb://` load-balanced routing |
| JWT Authentication | Spring OAuth2 Resource Server + Keycloak `issuer-uri` |
| Role-Based Authorization | `SecurityConfig.java` in API Gateway — `hasRole()` per HTTP method |
| Keycloak Role Extraction | Custom `JwtAuthenticationConverter` reading `realm_access.roles` |
| Service-to-Service Calls | OpenFeign client in Order Service calling User Service |
| JWT Forwarding | `FeignClientInterceptor` forwards Bearer token to User Service |
| Business Rules | Drools `.drl` file with 3 order validation rules |
| Exception Handling | `GlobalExceptionHandler` returns clean JSON, no stack traces |
| In-Memory Database | H2 with `create-drop` strategy and seed data on startup |
