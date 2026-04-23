package com.example.doctorservice.controller;

import com.example.doctorservice.entity.Doctor;
import com.example.doctorservice.entity.DoctorSchedule;
import com.example.doctorservice.entity.TimeSlot;
import com.example.doctorservice.service.DoctorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private static final Logger logger = LoggerFactory.getLogger(DoctorController.class);
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Doctor CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping
    public ResponseEntity<List<Doctor>> searchByName(
            @RequestParam(value = "name", defaultValue = "") String name) {
        return ResponseEntity.ok(doctorService.searchByName(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Doctor> createDoctor(@Valid @RequestBody Doctor doctor) {
        Doctor created = doctorService.createDoctor(doctor);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(
            @PathVariable Long id, @Valid @RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, doctor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<Doctor>> getAvailableDoctors() {
        return ResponseEntity.ok(doctorService.getAvailableDoctors());
    }

    @GetMapping("/specialization")
    public ResponseEntity<List<Doctor>> getBySpecialization(@RequestParam String name) {
        return ResponseEntity.ok(doctorService.getBySpecialization(name));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Availability check (called by Booking Service)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Check if a doctor is available for a given time slot.
     * GET /doctors/{id}/availability?timeSlotId=1
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable Long id, @RequestParam Long timeSlotId) {
        boolean available = doctorService.checkAvailability(id, timeSlotId);
        return ResponseEntity.ok(Map.of(
                "doctorId", id,
                "timeSlotId", timeSlotId,
                "available", available
        ));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // KEY API: Bệnh nhân chọn ca → hiện danh sách bác sĩ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get all doctors working in a specific time slot.
     * GET /doctors/by-timeslot/{timeSlotId}
     *
     * Flow: Bệnh nhân chọn ca khám → gọi API này → hiện danh sách bác sĩ
     */
    @GetMapping("/by-timeslot/{timeSlotId}")
    public ResponseEntity<List<Doctor>> getDoctorsByTimeSlot(@PathVariable Long timeSlotId) {
        logger.info("getDoctorsByTimeSlot - timeSlotId: {}", timeSlotId);
        return ResponseEntity.ok(doctorService.getDoctorsByTimeSlot(timeSlotId));
    }

    /**
     * Get all time slots for a specific doctor.
     * GET /doctors/{id}/timeslots
     */
    @GetMapping("/{id}/timeslots")
    public ResponseEntity<List<TimeSlot>> getTimeSlotsByDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getTimeSlotsByDoctor(id));
    }

    /**
     * Assign a doctor to a time slot.
     * POST /doctors/{doctorId}/timeslots/{timeSlotId}
     */
    @PostMapping("/{doctorId}/timeslots/{timeSlotId}")
    public ResponseEntity<DoctorSchedule> assignDoctorToTimeSlot(
            @PathVariable Long doctorId, @PathVariable Long timeSlotId) {
        DoctorSchedule schedule = doctorService.assignDoctorToTimeSlot(doctorId, timeSlotId);
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule);
    }
}
