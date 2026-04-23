package com.example.bookingservice.dto;

import com.example.bookingservice.entity.Ticket;

import java.time.LocalDate;

/**
 * DTO for returning ticket data in API responses.
 * Serializes the state as a plain string instead of a complex object.
 */
public class TicketResponse {

    private Long id;
    private Long patientId;
    private Long doctorId;
    private String name;
    private String phone;
    private String description;
    private LocalDate date;
    private Long timeSlotId;
    private String state;

    public TicketResponse() {
    }

    /**
     * Factory method to build a response from a Ticket entity.
     */
    public static TicketResponse fromEntity(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setPatientId(ticket.getPatientId());
        response.setDoctorId(ticket.getDoctorId());
        response.setName(ticket.getName());
        response.setPhone(ticket.getPhone());
        response.setDescription(ticket.getDescription());
        response.setDate(ticket.getDate());
        response.setTimeSlotId(ticket.getTimeSlotId());
        response.setState(ticket.getStateName());
        return response;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
