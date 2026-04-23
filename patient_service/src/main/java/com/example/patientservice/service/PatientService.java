package com.example.patientservice.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.patientservice.entity.Patient;
import com.example.patientservice.exception.ResourceNotFoundException;
import com.example.patientservice.repository.PatientRepository;

@Service
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);
    private final PatientRepository patientRepository;
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> searchByName(String name) {
        logger.debug("searchByName - Searching for: {}", name);
        List<Patient> result = patientRepository.findByTenContaining(name);
        logger.debug("searchByName - Found {} patients", result.size());
        return result;
    }

    public Patient getById(int id) {
        logger.debug("getById - Finding patient with id: {}", id);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("getById - Patient not found: id {}", id);
                    return new ResourceNotFoundException("Patient", id);
                });
        logger.debug("getById - Found patient: {}", patient.getTen());
        return patient;
    }

    public Patient getByMa(String ma) {
        logger.debug("getByMa - Finding patient with ma: {}", ma);
        return patientRepository.findByMa(ma)
                .orElseThrow(() -> {
                    logger.warn("getByMa - Patient not found: ma {}", ma);
                    return new ResourceNotFoundException("Bệnh nhân với mã " + ma + " không tồn tại.");
                });
    }

    public Patient createPatient(Patient patient) {
        logger.info("createPatient - Creating patient: ten={}, ma={}, SDT={}", 
            patient.getTen(), patient.getMa(), patient.getSDT());
        validatePatient(patient);
        checkDuplicateMa(patient.getMa(), null);
        checkDuplicateSDT(patient.getSDT(), null);
        Patient created = patientRepository.save(patient);
        logger.info("createPatient - Success: id={}, ten={}", created.getId(), created.getTen());
        return created;
    }


    public Patient updatePatient(int id, Patient updated) {
        logger.info("updatePatient - Updating patient id={}, ten={}, ma={}, SDT={}", 
            id, updated.getTen(), updated.getMa(), updated.getSDT());
        Patient existing = getById(id);
        validatePatient(updated);
        checkDuplicateMa(updated.getMa(), id);
        checkDuplicateSDT(updated.getSDT(), id);

        existing.setTen(updated.getTen());
        existing.setMa(updated.getMa());
        existing.setSDT(updated.getSDT());

        Patient result = patientRepository.save(existing);
        logger.info("updatePatient - Success: id={}", id);
        return result;
    }

    public void deletePatient(int id) {
        logger.info("deletePatient - Deleting patient id={}", id);
        // Ensure the patient exists before deleting
        getById(id);
        patientRepository.deleteById(id);
        logger.info("deletePatient - Success: id={}", id);
    }

    
    private void validatePatient(Patient patient) {
        logger.debug("validatePatient - Validating patient: ten={}, ma={}, SDT={}", 
            patient.getTen(), patient.getMa(), patient.getSDT());
        if (patient.getTen() == null || patient.getTen().isBlank()) {
            logger.warn("validatePatient - Validation failed: ten is blank");
            throw new IllegalArgumentException("Tên bệnh nhân không được để trống.");
        }
        if (patient.getSDT() == null || patient.getSDT().isBlank()) {
            logger.warn("validatePatient - Validation failed: SDT is blank");
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }
        if (patient.getMa() == null || patient.getMa().isBlank()) {
            logger.warn("validatePatient - Validation failed: ma is blank");
            throw new IllegalArgumentException("Mã bệnh nhân không được để trống.");
        }
        logger.debug("validatePatient - Validation passed");
    }

    
    private void checkDuplicateMa(String ma, Integer excludeId) {
        logger.debug("checkDuplicateMa - Checking duplicate for ma={}, excludeId={}", ma, excludeId);
        patientRepository.findByMa(ma).ifPresent(patient -> {
            if (excludeId == null || patient.getId() != excludeId) {
                logger.warn("checkDuplicateMa - Duplicate found: ma={}", ma);
                throw new IllegalArgumentException("Mã bệnh nhân \"" + ma + "\" đã tồn tại.");
            }
        });
        logger.debug("checkDuplicateMa - No duplicate found");
    }

    
    private void checkDuplicateSDT(String SDT, Integer excludeId) {
        logger.debug("checkDuplicateSDT - Checking duplicate for SDT={}, excludeId={}", SDT, excludeId);
        patientRepository.findBySDT(SDT).ifPresent(patient -> {
            if (excludeId == null || patient.getId() != excludeId) {
                logger.warn("checkDuplicateSDT - Duplicate found: SDT={}", SDT);
                throw new IllegalArgumentException("Số điện thoại \"" + SDT + "\" đã tồn tại.");
            }
        });
        logger.debug("checkDuplicateSDT - No duplicate found");
    }
}
