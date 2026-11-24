package com.genapp.repo;

import com.genapp.model.Motor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MotorRepository extends JpaRepository<Motor, Integer> {
}
