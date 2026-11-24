package com.genapp.repo;

import com.genapp.model.Endowment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EndowmentRepository extends JpaRepository<Endowment, Integer> {
}
