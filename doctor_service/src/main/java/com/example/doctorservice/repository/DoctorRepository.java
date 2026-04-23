package com.example.doctorservice.repository;

import com.example.doctorservice.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByNameContaining(String name);

    Optional<Doctor> findByCode(String code);

    Optional<Doctor> findByPhone(String phone);

    List<Doctor> findBySpecialization(String specialization);

    List<Doctor> findByAvailableTrue();
}
