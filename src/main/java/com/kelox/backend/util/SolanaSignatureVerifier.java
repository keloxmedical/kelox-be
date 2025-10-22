package com.kelox.backend.util;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.PublicKey;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Slf4j
public class SolanaSignatureVerifier {
    
    /**
     * Verify Solana signature from Privy
     * 
     * @param message The original message that was signed
     * @param signature The signature (base64 or hex encoded)
     * @param publicKey The public key (wallet address) as base58 string
     * @return true if signature is valid
     */
    public boolean verifySignature(String message, String signature, String publicKey) {
        log.info("Verifying signature for wallet: {}", publicKey);
        log.debug("Message: {}", message);
        log.debug("Signature length: {}", signature.length());
        
        try {
            // Parse the public key
            PublicKey pubKey = new PublicKey(publicKey);
            byte[] publicKeyBytes = pubKey.toByteArray();
            
            log.debug("Public key bytes length: {}", publicKeyBytes.length);
            
            // Try to decode signature - could be base64 or hex
            byte[] signatureBytes = decodeSignature(signature);
            
            if (signatureBytes == null) {
                log.error("Failed to decode signature");
                return false;
            }
            
            log.debug("Signature bytes length: {}", signatureBytes.length);
            
            // Convert message to bytes
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            
            log.debug("Message bytes length: {}", messageBytes.length);
            
            // Verify using TweetNacl with ed25519
            // Create a Signature instance for verification
            org.p2p.solanaj.utils.TweetNaclFast.Signature sigVerifier = 
                new org.p2p.solanaj.utils.TweetNaclFast.Signature(publicKeyBytes, new byte[64]);
            
            // Verify the detached signature
            boolean isValid = sigVerifier.detached_verify(messageBytes, signatureBytes);
            
            log.info("Signature verification result: {}", isValid);
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Error verifying Solana signature", e);
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Decode signature from base64, base58, or hex
     */
    private byte[] decodeSignature(String signature) {
        // Try base58 first (Solana native format)
        try {
            byte[] decoded = decodeBase58(signature);
            if (decoded != null && decoded.length == 64) {
                log.debug("Decoded signature as base58");
                return decoded;
            }
        } catch (Exception e) {
            log.debug("Not base58, trying other formats...");
        }
        
        // Try base64
        try {
            byte[] decoded = Base64.getDecoder().decode(signature);
            log.debug("Decoded signature as base64");
            return decoded;
        } catch (Exception e1) {
            log.debug("Not base64, trying hex...");
            
            // Try hex encoding
            try {
                byte[] decoded = hexToBytes(signature);
                log.debug("Decoded signature as hex");
                return decoded;
            } catch (Exception e2) {
                log.error("Failed to decode signature as base58, base64, or hex");
                return null;
            }
        }
    }
    
    /**
     * Decode base58 string (Solana format)
     */
    private byte[] decodeBase58(String base58) {
        try {
            return Base58.decode(base58);
        } catch (Exception e) {
            log.debug("Base58 decode failed: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert hex string to bytes
     */
    private byte[] hexToBytes(String hex) {
        if (hex.startsWith("0x")) {
            hex = hex.substring(2);
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
    
    /**
     * Extract wallet address from public key
     */
    public String extractWalletAddress(String publicKey) {
        try {
            PublicKey pubKey = new PublicKey(publicKey);
            return pubKey.toBase58();
        } catch (Exception e) {
            log.error("Error extracting wallet address: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate if string is a valid Solana wallet address
     */
    public boolean isValidWalletAddress(String address) {
        try {
            new PublicKey(address);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

