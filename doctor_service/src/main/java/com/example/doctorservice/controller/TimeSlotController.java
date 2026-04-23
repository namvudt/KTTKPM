package com.example.doctorservice.controller;

import com.example.doctorservice.entity.TimeSlot;
import com.example.doctorservice.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for TimeSlot management.
 * Patients call GET /timeslots to see available booking periods.
 */
@RestController
@RequestMapping("/timeslots")
public class TimeSlotController {

    private final DoctorService doctorService;

    public TimeSlotController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    /**
     * Get all available time slots.
     * GET /timeslots
     *
     * Patient opens the booking page → calls this → shows dropdown of time slots.
     */
    @GetMapping
    public ResponseEntity<List<TimeSlot>> getAllTimeSlots() {
        return ResponseEntity.ok(doctorService.getAllTimeSlots());
    }

    /**
     * Get a specific time slot by ID.
     * GET /timeslots/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TimeSlot> getById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getTimeSlotById(id));
    }
}
