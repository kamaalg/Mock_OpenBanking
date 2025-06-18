package com.example.mockopenbanking.repositories;

import com.example.mockopenbanking.dto.DocsResponse;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinRepository extends JpaRepository<DocsResponse, String> {

}
