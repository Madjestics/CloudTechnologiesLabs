package com.example.cloudlabs.repository;

import com.example.cloudlabs.entity.Director;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectorRepository extends JpaRepository<Director, Long> {
}
