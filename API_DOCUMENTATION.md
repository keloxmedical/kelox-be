# Kelox Medical App - API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication

All admin endpoints require the `X-Admin-Secret` header:

```
X-Admin-Secret: kelox-admin-secret-2024
```

**⚠️ IMPORTANT:** Change this secret in production! Update in `application.yml`:
```yaml
admin:
  secret-code: your-production-secret-here
```

---

## Admin APIs

### 👥 User Management

#### 1. Create User

**POST** `/admin/users`

Creates a new user with a wallet address.

**Headers:**
```
Content-Type: application/json
X-Admin-Secret: kelox-admin-secret-2024
```

**Request Body:**
```json
{
  "wallet": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb7"
}
```

**Response:** `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "wallet": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb7"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/admin/users \
  -H "Content-Type: application/json" \
  -H "X-Admin-Secret: kelox-admin-secret-2024" \
  -d '{
    "wallet": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb7"
  }'
```

---

#### 2. Get All Users

**GET** `/admin/users`

**Headers:**
```
X-Admin-Secret: kelox-admin-secret-2024
```

**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "wallet": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb7"
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/admin/users \
  -H "X-Admin-Secret: kelox-admin-secret-2024"
```

---

#### 3. Get User by ID

**GET** `/admin/users/{userId}`

**Headers:**
```
X-Admin-Secret: kelox-admin-secret-2024
```

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "wallet": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb7"
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/admin/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "X-Admin-Secret: kelox-admin-secret-2024"
```

---

#### 4. Get User by Wallet

**GET** `/admin/users/wallet/{wallet}`

**Headers:**
```
X-Admin-Secret: kelox-admin-secret-2024
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/admin/users/wallet/0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb7 \
  -H "X-Admin-Secret: kelox-admin-secret-2024"
```

---

### 🏥 Hospital Management

#### 1. Create Hospital Profile

**POST** `/admin/hospitals`

Creates a new hospital profile without an owner (owner can be assigned later).

**Headers:**
```
Content-Type: application/json
X-Admin-Secret: kelox-admin-secret-2024
```

**Request Body:**
```json
{
  "name": "St. Mary's Medical Center",
  "address": "123 Healthcare Blvd, Medical City, MC 12345",
  "companyName": "St. Mary's Healthcare Corporation",
  "contacts": [
    {
      "name": "Dr. John Smith",
      "position": "Chief Medical Officer",
      "email": "john.smith@stmarys.com",
      "phone": "+1-555-0123"
    },
    {
      "name": "Jane Doe",
      "position": "Administrator",
      "email": "jane.doe@stmarys.com",
      "phone": "+1-555-0124"
    }
  ]
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "St. Mary's Medical Center",
  "address": "123 Healthcare Blvd, Medical City, MC 12345",
  "companyName": "St. Mary's Healthcare Corporation",
  "ownerId": null,
  "ownerWallet": null,
  "contacts": [
    {
      "name": "Dr. John Smith",
      "position": "Chief Medical Officer",
      "email": "john.smith@stmarys.com",
      "phone": "+1-555-0123"
    },
    {
      "name": "Jane Doe",
      "position": "Administrator",
      "email": "jane.doe@stmarys.com",
      "phone": "+1-555-0124"
    }
  ]
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/admin/hospitals \
  -H "Content-Type: application/json" \
  -H "X-Admin-Secret: kelox-admin-secret-2024" \
  -d '{
    "name": "St. Mary'\''s Medical Center",
    "address": "123 Healthcare Blvd, Medical City, MC 12345",
    "companyName": "St. Mary'\''s Healthcare Corporation",
    "contacts": [
      {
        "name": "Dr. John Smith",
        "position": "Chief Medical Officer",
        "email": "john.smith@stmarys.com",
        "phone": "+1-555-0123"
      }
    ]
  }'
```

---

#### 2. Assign Owner to Hospital

**PUT** `/admin/hospitals/{hospitalId}/assign-owner`

Assigns a user as the owner of a hospital profile. Ensures 1:1 relationship - one user can only own one hospital.

**Headers:**
```
Content-Type: application/json
X-Admin-Secret: kelox-admin-secret-2024
```

**Request Body:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "St. Mary's Medical Center",
  "address": "123 Healthcare Blvd, Medical City, MC 12345",
  "companyName": "St. Mary's Healthcare Corporation",
  "ownerId": "550e8400-e29b-41d4-a716-446655440000",
  "ownerWallet": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb7",
  "contacts": [...]
}
```

**Business Rules:**
- ✅ User must exist
- ✅ Hospital must exist
- ✅ User cannot already be assigned to another hospital
- ✅ Hospital cannot already have an owner

**cURL Example:**
```bash
curl -X PUT http://localhost:8080/api/admin/hospitals/1/assign-owner \
  -H "Content-Type: application/json" \
  -H "X-Admin-Secret: kelox-admin-secret-2024" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

---

#### 3. Get All Hospitals

**GET** `/admin/hospitals`

**Headers:**
```
X-Admin-Secret: kelox-admin-secret-2024
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "St. Mary's Medical Center",
    "address": "123 Healthcare Blvd, Medical City, MC 12345",
    "companyName": "St. Mary's Healthcare Corporation",
    "ownerId": "550e8400-e29b-41d4-a716-446655440000",
    "ownerWallet": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb7",
    "contacts": [...]
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/admin/hospitals \
  -H "X-Admin-Secret: kelox-admin-secret-2024"
```

---

#### 4. Get Hospital by ID

**GET** `/admin/hospitals/{hospitalId}`

**Headers:**
```
X-Admin-Secret: kelox-admin-secret-2024
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/admin/hospitals/1 \
  -H "X-Admin-Secret: kelox-admin-secret-2024"
```

---

### 🏥 Health Check

#### Get Health Status

**GET** `/health`

**No authentication required.**

**Response:** `200 OK`
```json
{
  "status": "UP",
  "application": "Kelox Medical Backend"
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/health
```

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-10-14T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "User with wallet 0x123 already exists",
  "path": "/api/admin/users"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2024-10-14T15:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid admin secret code",
  "path": "/api/admin/hospitals"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-10-14T15:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Hospital profile not found with ID: 999",
  "path": "/api/admin/hospitals/999"
}
```

---

## Typical Workflow

### Scenario: Create a Hospital and Assign an Owner

**Step 1:** Create a user
```bash
curl -X POST http://localhost:8080/api/admin/users \
  -H "Content-Type: application/json" \
  -H "X-Admin-Secret: kelox-admin-secret-2024" \
  -d '{"wallet": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb7"}'
```

Response: Note the `id` (e.g., `550e8400-e29b-41d4-a716-446655440000`)

**Step 2:** Create a hospital profile
```bash
curl -X POST http://localhost:8080/api/admin/hospitals \
  -H "Content-Type: application/json" \
  -H "X-Admin-Secret: kelox-admin-secret-2024" \
  -d '{
    "name": "St. Mary'\''s Medical Center",
    "address": "123 Healthcare Blvd",
    "companyName": "St. Mary'\''s Healthcare",
    "contacts": [
      {
        "name": "Dr. John Smith",
        "position": "CMO",
        "email": "john@stmarys.com",
        "phone": "+1-555-0123"
      }
    ]
  }'
```

Response: Note the `id` (e.g., `1`)

**Step 3:** Assign the user as owner
```bash
curl -X PUT http://localhost:8080/api/admin/hospitals/1/assign-owner \
  -H "Content-Type: application/json" \
  -H "X-Admin-Secret: kelox-admin-secret-2024" \
  -d '{"userId": "550e8400-e29b-41d4-a716-446655440000"}'
```

✅ Done! The user now owns the hospital profile.

---

## Testing with Postman

### Import Collection

1. Create a new collection in Postman
2. Add environment variable:
   - `baseUrl`: `http://localhost:8080/api`
   - `adminSecret`: `kelox-admin-secret-2024`

3. Set headers for all admin requests:
   - `X-Admin-Secret`: `{{adminSecret}}`

### Example Requests

Save these as requests in your Postman collection:

1. **Create User**: `POST {{baseUrl}}/admin/users`
2. **Get All Users**: `GET {{baseUrl}}/admin/users`
3. **Create Hospital**: `POST {{baseUrl}}/admin/hospitals`
4. **Assign Owner**: `PUT {{baseUrl}}/admin/hospitals/1/assign-owner`
5. **Get All Hospitals**: `GET {{baseUrl}}/admin/hospitals`

---

## Security Notes

⚠️ **Production Deployment:**

1. **Change the admin secret** in `application.yml`
2. Consider using **environment variables**:
   ```yaml
   admin:
     secret-code: ${ADMIN_SECRET_CODE:default-dev-secret}
   ```

3. **Use HTTPS** in production
4. **Rotate secrets** regularly
5. Consider implementing **rate limiting**
6. Add **audit logging** for admin actions

---

## Database Schema

### Tables

- **users**: User accounts with wallet addresses
- **hospital_profiles**: Hospital information
- **contacts**: Hospital contact persons
- **flyway_schema_history**: Migration tracking

### Relationships

- User → Hospital Profile: **1:1** (one user owns one hospital)
- Hospital Profile → Contacts: **1:N** (one hospital has many contacts)

