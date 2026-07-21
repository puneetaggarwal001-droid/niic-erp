package com.niic.erp.production;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StyleRepository extends JpaRepository<Style, Long> {
    Optional<Style> findByCode(String code);
}
