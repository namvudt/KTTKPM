package com.example.patientservice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.patientservice.entity.Patient;
import com.example.patientservice.service.PatientService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/patients")
public class PatientController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<List<Patient>> searchByName(
            @RequestParam(value = "name", defaultValue = "") String name) {
        logger.info("SearchByName request - name: {}", name);
        List<Patient> patients = patientService.searchByName(name);
        logger.info("SearchByName - Found: {} patients", patients.size());
        return ResponseEntity.ok(patients);
    }

  
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getById(@PathVariable int id) {
        logger.info("GetById request - id: {}", id);
        Patient patient = patientService.getById(id);
        logger.info("GetById - Found patient: {}", patient.getTen());
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/code/{ma}")
    public ResponseEntity<Patient> getByMa(@PathVariable String ma) {
        logger.info("GetByMa request - ma: {}", ma);
        Patient patient = patientService.getByMa(ma);
        logger.info("GetByMa - Found patient: {}", patient.getTen());
        return ResponseEntity.ok(patient);
    }

    @PostMapping
    public ResponseEntity<Patient> createPatient(@Valid @RequestBody Patient patient) {
        logger.info("CreatePatient request - ten: {}, ma: {}, SDT: {}", 
            patient.getTen(), patient.getMa(), patient.getSDT());
        Patient created = patientService.createPatient(patient);
        logger.info("CreatePatient success - id: {}, ten: {}", created.getId(), created.getTen());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(
            @PathVariable int id,
            @Valid @RequestBody Patient patient) {
        logger.info("UpdatePatient request - id: {}, ten: {}, ma: {}, SDT: {}", 
            id, patient.getTen(), patient.getMa(), patient.getSDT());
        Patient updated = patientService.updatePatient(id, patient);
        logger.info("UpdatePatient success - id: {}", updated.getId());
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable int id) {
        logger.info("DeletePatient request - id: {}", id);
        patientService.deletePatient(id);
        logger.info("DeletePatient success - id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
