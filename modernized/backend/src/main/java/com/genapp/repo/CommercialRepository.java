package com.genapp.repo;

import com.genapp.model.Commercial;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommercialRepository extends JpaRepository<Commercial, Integer> {
    List<Commercial> findByZipcode(String zipcode);
}
