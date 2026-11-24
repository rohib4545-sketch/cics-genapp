package com.genapp.repo;

import com.genapp.model.Policy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy, Integer> {
    List<Policy> findByCustomerNumber(Integer customerNumber);
}
