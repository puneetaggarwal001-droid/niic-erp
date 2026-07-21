package com.niic.erp.attendance;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmpId(String empId);

    List<Employee> findByActiveTrue();

    boolean existsByAadharAndActiveTrueAndIdNot(String aadhar, Long id);

    boolean existsByPhoneAndActiveTrueAndIdNot(String phone, Long id);

    @Query("select e.empId from Employee e where e.empId like 'EMP-%'")
    List<String> findAllEmpIds();
}
