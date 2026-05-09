package com.example.patientservice.config;

import com.example.patientservice.entity.Patient;
import com.example.patientservice.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds initial patient data on first run.
 * Only inserts if the patients table is empty (safe for restart).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final PatientRepository patientRepository;

    public DataInitializer(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public void run(String... args) {
        if (patientRepository.count() > 0) {
            log.info("Patient data already exists, skipping initialization.");
            return;
        }

        log.info("═══ Seeding patient data ═══");

        patientRepository.save(new Patient("Nguyễn Văn Hùng",  "BN001", "0911111111"));
        patientRepository.save(new Patient("Trần Thị Mai",     "BN002", "0922222222"));
        patientRepository.save(new Patient("Lê Hoàng Nam",     "BN003", "0933333333"));
        patientRepository.save(new Patient("Phạm Thị Lan",     "BN004", "0944444444"));
        patientRepository.save(new Patient("Võ Minh Tuấn",     "BN005", "0955555555"));

        log.info("═══ Created 5 sample patients (BN001 → BN005) ═══");
    }
}
