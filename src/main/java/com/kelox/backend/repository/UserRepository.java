package com.kelox.backend.repository;

import com.kelox.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByPrivyId(String privyId);
    
    Optional<User> findBySolanaWallet(String solanaWallet);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEthereumWallet(String ethereumWallet);
    
    boolean existsByPrivyId(String privyId);
    
    boolean existsBySolanaWallet(String solanaWallet);
    
    boolean existsByEmail(String email);
}

