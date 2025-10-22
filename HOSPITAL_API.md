# Hospital Profile API

## Get My Hospital Profile

Get the hospital profile for the currently authenticated user (if they own a hospital).

### Endpoint
```
GET /api/hospitals/my-profile
```

### Authentication
Requires JWT token in Authorization header.

### Request Headers
```
Authorization: Bearer {your_jwt_token}
```

### Response

#### If User Owns a Hospital (200 OK):
```json
{
  "id": 1,
  "name": "St. Mary's Medical Center",
  "address": "123 Healthcare Blvd, Medical City, MC 12345",
  "companyName": "St. Mary's Healthcare Corporation",
  "ownerId": "550e8400-e29b-41d4-a716-446655440000",
  "ownerEmail": "user@example.com",
  "ownerSolanaWallet": "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM",
  "contacts": [
    {
      "name": "Dr. John Smith",
      "position": "Chief Medical Officer",
      "email": "john.smith@stmarys.com",
      "phone": "+1-555-0123"
    }
  ]
}
```

#### If User Does NOT Own a Hospital (404 Not Found):
```json
{
  "timestamp": "2024-10-14T17:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "No hospital profile found for owner ID: 550e8400-e29b-41d4-a716-446655440000",
  "path": "/api/hospitals/my-profile"
}
```

#### If Token is Invalid/Expired (401 Unauthorized):
```json
{
  "timestamp": "2024-10-14T17:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/api/hospitals/my-profile"
}
```

---

## Frontend Integration

### React/Next.js Example

```typescript
import { useState, useEffect } from 'react';

const MyHospital = () => {
  const [hospital, setHospital] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchMyHospital();
  }, []);

  const fetchMyHospital = async () => {
    try {
      // Get token from localStorage
      const token = localStorage.getItem('kelox_token');
      
      if (!token) {
        setError('Not authenticated');
        setLoading(false);
        return;
      }

      const response = await fetch('http://localhost:8080/api/hospitals/my-profile', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        setHospital(data);
      } else if (response.status === 404) {
        // User doesn't own a hospital
        setHospital(null);
      } else {
        throw new Error('Failed to fetch hospital profile');
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;
  
  if (!hospital) {
    return <div>You don't own a hospital yet.</div>;
  }

  return (
    <div>
      <h1>{hospital.name}</h1>
      <p>{hospital.address}</p>
      <p>Company: {hospital.companyName}</p>
      
      <h2>Contacts</h2>
      <ul>
        {hospital.contacts?.map((contact, idx) => (
          <li key={idx}>
            {contact.name} - {contact.position}
            <br />
            {contact.email} | {contact.phone}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default MyHospital;
```

### Using with React Query

```typescript
import { useQuery } from '@tanstack/react-query';

const fetchMyHospital = async () => {
  const token = localStorage.getItem('kelox_token');
  
  const response = await fetch('http://localhost:8080/api/hospitals/my-profile', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    if (response.status === 404) {
      return null; // User doesn't own a hospital
    }
    throw new Error('Failed to fetch hospital');
  }

  return response.json();
};

const MyHospital = () => {
  const { data: hospital, isLoading, error } = useQuery({
    queryKey: ['myHospital'],
    queryFn: fetchMyHospital,
    retry: false
  });

  // ... rest of component
};
```

### Custom Hook

```typescript
import { useState, useEffect } from 'react';

export const useMyHospital = () => {
  const [hospital, setHospital] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchHospital = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('kelox_token');
      
      const response = await fetch('http://localhost:8080/api/hospitals/my-profile', {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (response.ok) {
        const data = await response.json();
        setHospital(data);
      } else if (response.status === 404) {
        setHospital(null);
      } else {
        throw new Error('Failed to fetch');
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHospital();
  }, []);

  return { hospital, loading, error, refetch: fetchHospital };
};

// Usage
const MyComponent = () => {
  const { hospital, loading } = useMyHospital();
  
  if (loading) return <div>Loading...</div>;
  
  return hospital ? (
    <div>{hospital.name}</div>
  ) : (
    <div>No hospital</div>
  );
};
```

---

## cURL Examples

### Get My Hospital Profile

```bash
# Replace YOUR_JWT_TOKEN with actual token
curl -X GET http://localhost:8080/api/hospitals/my-profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Postman

### Request
- **Method:** GET
- **URL:** `http://localhost:8080/api/hospitals/my-profile`
- **Headers:**
  - `Authorization`: `Bearer {your_token_from_login}`

### Steps
1. Login first using `/api/auth/login`
2. Copy the `token` from the response
3. Use that token in the `Authorization` header
4. Send GET request to `/api/hospitals/my-profile`

---

## Use Cases

### Check if User is Hospital Owner on Login
```typescript
const handleLogin = async () => {
  const loginResponse = await login();
  
  // Check if user owns a hospital from login response
  if (loginResponse.hospitalProfile) {
    console.log('User owns hospital:', loginResponse.hospitalProfile.name);
    // Redirect to hospital dashboard
  } else {
    console.log('User is not a hospital owner');
    // Show regular user dashboard
  }
};
```

### Lazy Load Hospital Profile
```typescript
// Only fetch when user navigates to hospital section
const HospitalDashboard = () => {
  const { hospital, loading } = useMyHospital();
  
  if (loading) return <Spinner />;
  
  if (!hospital) {
    return <NoHospitalMessage />;
  }
  
  return <HospitalDetails hospital={hospital} />;
};
```

### Real-time Updates
```typescript
// Refetch hospital after admin assigns ownership
const refetchHospital = async () => {
  const { hospital } = await fetchMyHospital();
  if (hospital) {
    toast.success('You are now a hospital owner!');
    navigate('/hospital/dashboard');
  }
};
```

---

## Notes

- ✅ Token is validated on each request
- ✅ Returns 404 if user doesn't own a hospital (not an error, just no hospital)
- ✅ Returns full hospital details including contacts
- ✅ No need to pass user ID - extracted from JWT token
- ✅ Works with the token received from `/api/auth/login`

