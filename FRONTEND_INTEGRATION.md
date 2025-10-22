# Frontend Integration Guide

## Login API - Expected Request Format

### ❌ WRONG - Sending Objects
```javascript
// DON'T send objects like this:
const body = {
  privyId: user,                    // ❌ Object
  email: user.email,                // ❌ Object
  solanaWallet: wallet,             // ❌ Object
  ethereumWallet: ethWallet,        // ❌ Object
  message: "Sign this...",
  signature: signatureObject        // ❌ Object
}
```

### ✅ CORRECT - Extract String Values
```javascript
// DO extract string values:
const body = {
  privyId: user.id,                           // ✅ String: "did:privy:..."
  email: user.email?.address,                 // ✅ String: "user@example.com"
  solanaWallet: solanaWallet.address,         // ✅ String: "9Wz...WM"
  ethereumWallet: ethWallet?.address || null, // ✅ String or null
  message: "Sign this message...",            // ✅ String
  signature: signatureBase64                  // ✅ String (base64)
}
```

## Complete Next.js Example

```typescript
import { usePrivy } from '@privy-io/react-auth';
import { useWallets } from '@privy-io/react-auth';

export default function LoginButton() {
  const { user, authenticated } = usePrivy();
  const { wallets } = useWallets();

  const handleLogin = async () => {
    if (!authenticated || !user) {
      console.error('User not authenticated with Privy');
      return;
    }

    // Get Solana wallet
    const solanaWallet = wallets.find(
      (wallet) => wallet.walletClientType === 'privy' && 
                  wallet.chainType === 'solana'
    );

    if (!solanaWallet) {
      console.error('No Solana wallet found');
      return;
    }

    // Get Ethereum wallet (optional)
    const ethWallet = wallets.find(
      (wallet) => wallet.chainType === 'ethereum'
    );

    // Message to sign
    const message = "Sign this message to authenticate with Kelox Medical";

    try {
      // Sign message with Solana wallet using Privy
      // Privy returns signature in different formats depending on the method
      let signatureBase64;
      
      // Method 1: Using signMessage (returns Uint8Array)
      const signatureBytes = await solanaWallet.signMessage(message);
      signatureBase64 = btoa(String.fromCharCode(...new Uint8Array(signatureBytes)));
      
      // Alternative Method 2: If signature is already a string
      // signatureBase64 = signatureResponse;
      
      // Alternative Method 3: If signature is hex string
      // signatureBase64 = signatureResponse; // Backend will handle hex automatically

      // Prepare request body with STRING values only
      const requestBody = {
        privyId: user.id,                                    // String
        email: user.email?.address || '',                    // String
        solanaWallet: solanaWallet.address,                  // String
        ethereumWallet: ethWallet?.address || null,          // String or null
        message: message,                                     // String
        signature: signatureBase64                            // String (base64)
      };

      console.log('Sending to backend:', requestBody);

      // Call backend
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody)
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Login failed: ${errorText}`);
      }

      const data = await response.json();
      
      console.log('Login successful:', data);
      
      // Store JWT token
      localStorage.setItem('kelox_token', data.token);
      localStorage.setItem('kelox_user', JSON.stringify(data));

      return data;

    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  };

  return (
    <button onClick={handleLogin}>
      Login to Kelox
    </button>
  );
}
```

## Expected Backend Request Format

```json
{
  "privyId": "did:privy:clxyz123abc",
  "email": "user@example.com",
  "solanaWallet": "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM",
  "ethereumWallet": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb7",
  "message": "Sign this message to authenticate with Kelox Medical",
  "signature": "base64_encoded_signature_string"
}
```

## Backend Response Format

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "privyId": "did:privy:clxyz123abc",
  "email": "user@example.com",
  "solanaWallet": "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM",
  "ethereumWallet": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb7",
  "newUser": true
}
```

## Common Mistakes

### 1. Sending User Object Instead of ID
```javascript
// ❌ WRONG
privyId: user

// ✅ CORRECT
privyId: user.id
```

### 2. Sending Email Object Instead of String
```javascript
// ❌ WRONG
email: user.email

// ✅ CORRECT
email: user.email?.address || user.email
```

### 3. Sending Wallet Object Instead of Address
```javascript
// ❌ WRONG
solanaWallet: wallet

// ✅ CORRECT
solanaWallet: wallet.address
```

### 4. Signature Not Base64 Encoded
```javascript
// ❌ WRONG (sending Uint8Array or object)
signature: signatureBytes

// ✅ CORRECT (base64 string)
signature: btoa(String.fromCharCode(...signatureBytes))
```

## Debugging Tips

### Check What You're Sending
```javascript
console.log('Request body:', JSON.stringify(requestBody, null, 2));
console.log('Types:', {
  privyId: typeof requestBody.privyId,
  email: typeof requestBody.email,
  solanaWallet: typeof requestBody.solanaWallet,
  signature: typeof requestBody.signature
});
```

All fields should be `"string"` type!

### Check Backend Logs
Look for the actual JSON received:
```
2025-10-14T17:26:22.913+03:00  WARN ... JSON parse error: Cannot deserialize...
```

This means one of your fields is an object instead of a string.

## Using the Token

After successful login, use the token for authenticated requests:

```javascript
const token = localStorage.getItem('kelox_token');

const response = await fetch('http://localhost:8080/api/some-endpoint', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

