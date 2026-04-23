package com.example.doctorservice.config;

import com.example.doctorservice.entity.Doctor;
import com.example.doctorservice.entity.DoctorSchedule;
import com.example.doctorservice.entity.TimeSlot;
import com.example.doctorservice.repository.DoctorRepository;
import com.example.doctorservice.repository.DoctorScheduleRepository;
import com.example.doctorservice.repository.TimeSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds initial data: time slots and sample doctors on first run.
 * Only inserts if tables are empty (safe for restart).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final TimeSlotRepository timeSlotRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    public DataInitializer(TimeSlotRepository timeSlotRepository,
                           DoctorRepository doctorRepository,
                           DoctorScheduleRepository doctorScheduleRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
    }

    @Override
    public void run(String... args) {
        // Only seed if time_slots table is empty
        if (timeSlotRepository.count() > 0) {
            log.info("Data already exists, skipping initialization.");
            return;
        }

        log.info("═══ Seeding initial data ═══");

        // ─── Time Slots ────────────────────────────────────────────────────────
        TimeSlot morning = timeSlotRepository.save(new TimeSlot("Ca sáng", "09:00", "12:00"));
        TimeSlot afternoon = timeSlotRepository.save(new TimeSlot("Ca chiều", "14:00", "16:00"));
        log.info("Created time slots: {} (id={}), {} (id={})",
                morning.getName(), morning.getId(),
                afternoon.getName(), afternoon.getId());

        // ─── Sample Doctors ────────────────────────────────────────────────────
        Doctor doc1 = doctorRepository.save(new Doctor("Nguyễn Văn An", "BS001", "0901111111", "Nội khoa"));
        Doctor doc2 = doctorRepository.save(new Doctor("Trần Thị Bình", "BS002", "0902222222", "Ngoại khoa"));
        Doctor doc3 = doctorRepository.save(new Doctor("Lê Minh Cường", "BS003", "0903333333", "Nhi khoa"));
        Doctor doc4 = doctorRepository.save(new Doctor("Phạm Thị Dung", "BS004", "0904444444", "Nội khoa"));
        log.info("Created {} sample doctors", 4);

        // ─── Assign doctors to time slots ──────────────────────────────────────
        // Ca sáng: BS An, BS Bình, BS Cường
        doctorScheduleRepository.save(new DoctorSchedule(doc1, morning));
        doctorScheduleRepository.save(new DoctorSchedule(doc2, morning));
        doctorScheduleRepository.save(new DoctorSchedule(doc3, morning));

        // Ca chiều: BS Bình, BS Cường, BS Dung
        doctorScheduleRepository.save(new DoctorSchedule(doc2, afternoon));
        doctorScheduleRepository.save(new DoctorSchedule(doc3, afternoon));
        doctorScheduleRepository.save(new DoctorSchedule(doc4, afternoon));

        log.info("═══ Data initialization complete ═══");
        log.info("Ca sáng (id={}): BS An, BS Bình, BS Cường", morning.getId());
        log.info("Ca chiều (id={}): BS Bình, BS Cường, BS Dung", afternoon.getId());
    }
}
