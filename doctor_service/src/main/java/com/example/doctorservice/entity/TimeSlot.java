package com.example.doctorservice.entity;

import jakarta.persistence.*;

/**
 * Entity representing a time slot for doctor appointments.
 * E.g., "Ca sáng 9h-12h", "Ca chiều 14h-16h"
 */
@Entity
@Table(name = "time_slots")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name, e.g., "Ca sáng" */
    @Column(nullable = false)
    private String name;

    /** Start time, e.g., "09:00" */
    @Column(nullable = false)
    private String startTime;

    /** End time, e.g., "12:00" */
    @Column(nullable = false)
    private String endTime;

    /**
     * Maximum number of patients that can be booked per doctor in this slot per day.
     * Default is 1 (one patient per doctor per slot).
     * Increase this to allow multiple patients per doctor in the same time slot.
     */
    @Column(nullable = false, columnDefinition = "INT NOT NULL DEFAULT 1")
    private int maxPatients = 1;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public TimeSlot() {}

    public TimeSlot(String name, String startTime, String endTime) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxPatients = 1;
    }

    public TimeSlot(String name, String startTime, String endTime, int maxPatients) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxPatients = maxPatients;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public int getMaxPatients() { return maxPatients; }
    public void setMaxPatients(int maxPatients) { this.maxPatients = maxPatients; }
}
