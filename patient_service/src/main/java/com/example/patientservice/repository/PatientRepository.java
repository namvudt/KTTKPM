package com.example.patientservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.patientservice.entity.Patient;

/**
 * Repository interface for Patient entity.
 * Provides CRUD operations and custom query methods via Spring Data JPA.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {

    /**
     * Find all patients whose name contains the given string (case-insensitive via LIKE).
     *
     * @param name partial name to search
     * @return list of matching patients
     */
    List<Patient> findByTenContaining(String name);

    /**
     * Find a patient by their primary key.
     *
     * @param id patient ID
     * @return Optional wrapping the found patient
     */
    Optional<Patient> findById(int id);

    /**
     * Find patients by phone number.
     *
     * @param SDT phone number to search
     * @return Optional wrapping the found patient
     */
    Optional<Patient> findBySDT(String SDT);

    /**
     * Find patients by patient code.
     *
     * @param ma patient code to search
     * @return Optional wrapping the found patient
     */
    Optional<Patient> findByMa(String ma);
}
