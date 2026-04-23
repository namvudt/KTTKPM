package com.example.bookingservice.entity;

import com.example.bookingservice.entity.state.NewState;
import com.example.bookingservice.entity.state.TicketState;
import com.example.bookingservice.entity.state.TicketStateConverter;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Ticket entity representing a booking request.
 * Uses the State Pattern to manage its lifecycle — all transition
 * logic is delegated to the current TicketState, with NO if-else.
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Long timeSlotId;

    @Convert(converter = TicketStateConverter.class)
    @Column(nullable = false)
    private TicketState state;

    // =============================================
    // Constructors
    // =============================================

    public Ticket() {
        this.state = new NewState();
    }

    public Ticket(Long patientId, Long doctorId, String name, String phone,
                  String description, LocalDate date, Long timeSlotId) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.date = date;
        this.timeSlotId = timeSlotId;
        this.state = new NewState();
    }

    // =============================================
    // State-delegated actions (NO if-else)
    // =============================================

    public void requestConfirm() {
        this.state = this.state.handleConfirm();
    }

    public void cancel() {
        this.state = this.state.handleCancel();
    }

    public void approve() {
        this.state = this.state.handleApprove();
    }

    public void reject() {
        this.state = this.state.handleReject();
    }

    // =============================================
    // Getters and Setters
    // =============================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(Long timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public TicketState getState() {
        return state;
    }

    public void setState(TicketState state) {
        this.state = state;
    }

    /**
     * Convenience getter for serialization —
     * returns the state name as a simple string.
     */
    public String getStateName() {
        return state.getName();
    }
}
