# Debugging Privy Signature Issues

## Check Backend Logs

After restarting your Spring Boot app, you'll see detailed logs when authentication fails:

```
2025-10-14T17:26:22.913+03:00  INFO ... Verifying signature for wallet: 9Wz...WM
2025-10-14T17:26:22.914+03:00  DEBUG ... Message: Sign this message...
2025-10-14T17:26:22.914+03:00  DEBUG ... Signature length: 88
2025-10-14T17:26:22.914+03:00  DEBUG ... Public key bytes length: 32
2025-10-14T17:26:22.914+03:00  DEBUG ... Signature bytes length: 64
2025-10-14T17:26:22.914+03:00  DEBUG ... Message bytes length: 45
2025-10-14T17:26:22.915+03:00  INFO ... Signature verification result: false
```

### What to Check:

1. **Signature bytes length should be 64** - If not, the encoding is wrong
2. **Public key bytes length should be 32** - Solana public keys are always 32 bytes
3. **Message must match exactly** - Same message on frontend and backend

## Frontend - Different Privy Signature Methods

### Method 1: Using `signMessage` (Recommended)
```typescript
import { useWallets } from '@privy-io/react-auth';

const { wallets } = useWallets();

const solanaWallet = wallets.find(w => 
  w.walletClientType === 'privy' && w.chainType === 'solana'
);

// Sign and encode
const message = "Sign this message to authenticate with Kelox Medical";
const signatureBytes = await solanaWallet.signMessage(message);

// Convert to base64
const signatureBase64 = btoa(
  String.fromCharCode(...new Uint8Array(signatureBytes))
);

console.log('Signature:', signatureBase64);
console.log('Signature length:', signatureBase64.length); // Should be ~88 chars
```

### Method 2: Using `sign` with Uint8Array
```typescript
const messageBytes = new TextEncoder().encode(message);
const signatureBytes = await solanaWallet.sign(messageBytes);

const signatureBase64 = btoa(
  String.fromCharCode(...new Uint8Array(signatureBytes))
);
```

### Method 3: If Privy Returns Hex String
```typescript
// Some Privy versions return hex string directly
const signatureHex = await solanaWallet.signMessage(message);

// Backend will handle hex automatically, send as-is
// OR convert to base64:
const signatureBytes = hexToBytes(signatureHex);
const signatureBase64 = btoa(String.fromCharCode(...signatureBytes));

function hexToBytes(hex) {
  const bytes = [];
  for (let i = 0; i < hex.length; i += 2) {
    bytes.push(parseInt(hex.substr(i, 2), 16));
  }
  return new Uint8Array(bytes);
}
```

## Complete Debug Frontend Code

```typescript
const handleLogin = async () => {
  try {
    const { user, wallets } = usePrivy();
    
    // Get Solana wallet
    const solanaWallet = wallets.find(w => 
      w.walletClientType === 'privy' && w.chainType === 'solana'
    );

    const message = "Sign this message to authenticate with Kelox Medical";
    
    console.log('=== SIGNATURE DEBUG ===');
    console.log('Message:', message);
    console.log('Wallet address:', solanaWallet.address);
    
    // Sign message
    const signatureBytes = await solanaWallet.signMessage(message);
    
    console.log('Signature type:', typeof signatureBytes);
    console.log('Signature:', signatureBytes);
    
    // Convert to base64
    let signatureBase64;
    if (signatureBytes instanceof Uint8Array) {
      signatureBase64 = btoa(String.fromCharCode(...signatureBytes));
    } else if (typeof signatureBytes === 'string') {
      // Already a string, might be hex or base64
      signatureBase64 = signatureBytes;
    } else {
      console.error('Unknown signature format:', signatureBytes);
      return;
    }
    
    console.log('Signature base64:', signatureBase64);
    console.log('Signature length:', signatureBase64.length);
    
    // Prepare request
    const requestBody = {
      privyId: user.id,
      email: user.email?.address,
      solanaWallet: solanaWallet.address,
      ethereumWallet: null,
      message: message,
      signature: signatureBase64
    };
    
    console.log('Request body:', requestBody);
    
    // Send to backend
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    });
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('Backend error:', errorText);
      throw new Error(errorText);
    }
    
    const data = await response.json();
    console.log('Login successful:', data);
    
    return data;
    
  } catch (error) {
    console.error('Login failed:', error);
  }
};
```

## Common Issues

### Issue 1: "Invalid Solana signature"
**Cause:** Message doesn't match or signature encoding is wrong

**Solution:**
1. Make sure the EXACT same message is used on frontend and backend
2. Try different encoding methods (see above)
3. Check backend logs for signature/message lengths

### Issue 2: Signature is not 64 bytes
**Cause:** Wrong encoding or Privy returns hex instead of bytes

**Solution:**
```typescript
// If signature looks like: "a1b2c3d4e5f6..."
// Convert hex to bytes first:
const signatureBytes = hexToBytes(signature);
const signatureBase64 = btoa(String.fromCharCode(...signatureBytes));
```

### Issue 3: Cannot read properties of undefined
**Cause:** Wallet not properly initialized

**Solution:**
```typescript
// Make sure wallet is ready
if (!solanaWallet || !solanaWallet.signMessage) {
  console.error('Solana wallet not ready');
  return;
}
```

## Test Without Signature Verification

For testing, you can temporarily bypass signature verification:

### Backend - Temporary Test Endpoint
Use the test endpoint:
```
POST http://localhost:8080/api/test/auth/generate-token/{solanaWallet}
```

This bypasses signature verification and generates a token directly.

### Frontend - Test Call
```typescript
const testLogin = async () => {
  const response = await fetch(
    `http://localhost:8080/api/test/auth/generate-token/${solanaWallet.address}`,
    { method: 'POST' }
  );
  const data = await response.json();
  console.log('Test token:', data);
};
```

**⚠️ Remove test endpoint before production!**

## Expected Signature Format

A valid Solana signature in base64:
- Length: ~88 characters
- Example: `4ZH8F2KiW9xE3gH7jK5mN8qP2rT6vY9zA3bC5dE7fG9hJ1kL3mN5oP7qR9sT1uV3wX5yZ7aB9cD1eF3gH5iJ7k==`
- When decoded: 64 bytes

Check your signature matches this format!

