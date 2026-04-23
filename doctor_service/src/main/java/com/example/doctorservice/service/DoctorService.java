package com.example.doctorservice.service;

import com.example.doctorservice.entity.Doctor;
import com.example.doctorservice.entity.DoctorSchedule;
import com.example.doctorservice.entity.TimeSlot;
import com.example.doctorservice.exception.ResourceNotFoundException;
import com.example.doctorservice.repository.DoctorRepository;
import com.example.doctorservice.repository.DoctorScheduleRepository;
import com.example.doctorservice.repository.TimeSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorService.class);
    private final DoctorRepository doctorRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    public DoctorService(DoctorRepository doctorRepository,
                         TimeSlotRepository timeSlotRepository,
                         DoctorScheduleRepository doctorScheduleRepository) {
        this.doctorRepository = doctorRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Doctor CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    public List<Doctor> searchByName(String name) {
        logger.debug("searchByName - Searching for: {}", name);
        return doctorRepository.findByNameContaining(name);
    }

    public Doctor getById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
    }

    public Doctor createDoctor(Doctor doctor) {
        logger.info("createDoctor - Creating: name={}, code={}", doctor.getName(), doctor.getCode());
        validateDoctor(doctor);
        checkDuplicateCode(doctor.getCode(), null);
        checkDuplicatePhone(doctor.getPhone(), null);
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Long id, Doctor updated) {
        logger.info("updateDoctor - Updating doctor id={}", id);
        Doctor existing = getById(id);
        validateDoctor(updated);
        checkDuplicateCode(updated.getCode(), id);
        checkDuplicatePhone(updated.getPhone(), id);

        existing.setName(updated.getName());
        existing.setCode(updated.getCode());
        existing.setPhone(updated.getPhone());
        existing.setSpecialization(updated.getSpecialization());
        existing.setAvailable(updated.isAvailable());

        return doctorRepository.save(existing);
    }

    public void deleteDoctor(Long id) {
        logger.info("deleteDoctor - Deleting doctor id={}", id);
        getById(id);
        doctorRepository.deleteById(id);
    }

    public List<Doctor> getAvailableDoctors() {
        return doctorRepository.findByAvailableTrue();
    }

    public List<Doctor> getBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TimeSlot
    // ═══════════════════════════════════════════════════════════════════════════

    public List<TimeSlot> getAllTimeSlots() {
        return timeSlotRepository.findAll();
    }

    public TimeSlot getTimeSlotById(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", id));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Schedule: Doctor ↔ TimeSlot
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get all doctors working in a specific time slot.
     * This is the KEY API: patient chọn ca → hiện ra danh sách bác sĩ.
     */
    public List<Doctor> getDoctorsByTimeSlot(Long timeSlotId) {
        logger.info("getDoctorsByTimeSlot - timeSlotId={}", timeSlotId);
        // Verify timeSlot exists
        getTimeSlotById(timeSlotId);

        List<DoctorSchedule> schedules = doctorScheduleRepository.findByTimeSlotId(timeSlotId);
        List<Doctor> doctors = schedules.stream()
                .map(DoctorSchedule::getDoctor)
                .filter(Doctor::isAvailable)
                .toList();

        logger.info("getDoctorsByTimeSlot - Found {} available doctors for timeSlot {}", doctors.size(), timeSlotId);
        return doctors;
    }

    /**
     * Get all time slots for a specific doctor.
     */
    public List<TimeSlot> getTimeSlotsByDoctor(Long doctorId) {
        getById(doctorId);
        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(doctorId);
        return schedules.stream()
                .map(DoctorSchedule::getTimeSlot)
                .toList();
    }

    /**
     * Assign a doctor to a time slot.
     */
    public DoctorSchedule assignDoctorToTimeSlot(Long doctorId, Long timeSlotId) {
        logger.info("assignDoctorToTimeSlot - doctorId={}, timeSlotId={}", doctorId, timeSlotId);
        Doctor doctor = getById(doctorId);
        TimeSlot timeSlot = getTimeSlotById(timeSlotId);

        if (doctorScheduleRepository.existsByDoctorIdAndTimeSlotId(doctorId, timeSlotId)) {
            throw new IllegalArgumentException(
                    "Bác sĩ " + doctor.getName() + " đã được phân ca " + timeSlot.getName());
        }

        DoctorSchedule schedule = new DoctorSchedule(doctor, timeSlot);
        return doctorScheduleRepository.save(schedule);
    }

    /**
     * Check if a doctor is available for a given time slot.
     * Used by Booking Service.
     */
    public boolean checkAvailability(Long doctorId, Long timeSlotId) {
        logger.info("checkAvailability - doctorId={}, timeSlotId={}", doctorId, timeSlotId);
        Doctor doctor = getById(doctorId);

        if (!doctor.isAvailable()) {
            return false;
        }

        // Check if the doctor is assigned to this time slot
        boolean assigned = doctorScheduleRepository.existsByDoctorIdAndTimeSlotId(doctorId, timeSlotId);
        logger.info("checkAvailability - Doctor {} assigned to timeSlot {}: {}", doctorId, timeSlotId, assigned);
        return assigned;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Private validation helpers
    // ═══════════════════════════════════════════════════════════════════════════

    private void validateDoctor(Doctor doctor) {
        if (doctor.getName() == null || doctor.getName().isBlank())
            throw new IllegalArgumentException("Tên bác sĩ không được để trống.");
        if (doctor.getPhone() == null || doctor.getPhone().isBlank())
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        if (doctor.getCode() == null || doctor.getCode().isBlank())
            throw new IllegalArgumentException("Mã bác sĩ không được để trống.");
        if (doctor.getSpecialization() == null || doctor.getSpecialization().isBlank())
            throw new IllegalArgumentException("Chuyên khoa không được để trống.");
    }

    private void checkDuplicateCode(String code, Long excludeId) {
        doctorRepository.findByCode(code).ifPresent(doctor -> {
            if (excludeId == null || !doctor.getId().equals(excludeId))
                throw new IllegalArgumentException("Mã bác sĩ \"" + code + "\" đã tồn tại.");
        });
    }

    private void checkDuplicatePhone(String phone, Long excludeId) {
        doctorRepository.findByPhone(phone).ifPresent(doctor -> {
            if (excludeId == null || !doctor.getId().equals(excludeId))
                throw new IllegalArgumentException("Số điện thoại \"" + phone + "\" đã tồn tại.");
        });
    }
}
