package com.example.doctorservice.entity;

import jakarta.persistence.*;

/**
 * Join entity: which doctor works in which time slot.
 * A doctor can work in multiple time slots.
 * A time slot can have multiple doctors.
 */
@Entity
@Table(name = "doctor_schedules",
       uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "time_slot_id"}))
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public DoctorSchedule() {}

    public DoctorSchedule(Doctor doctor, TimeSlot timeSlot) {
        this.doctor = doctor;
        this.timeSlot = timeSlot;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public TimeSlot getTimeSlot() { return timeSlot; }
    public void setTimeSlot(TimeSlot timeSlot) { this.timeSlot = timeSlot; }
}
