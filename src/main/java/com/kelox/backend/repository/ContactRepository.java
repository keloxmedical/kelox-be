package com.kelox.backend.repository;

import com.kelox.backend.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    List<Contact> findByHospitalProfileId(Long hospitalProfileId);
    
    List<Contact> findByEmail(String email);
}

