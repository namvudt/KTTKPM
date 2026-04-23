package com.example.bookingservice.service;

import com.example.bookingservice.client.DoctorServiceClient;
import com.example.bookingservice.client.PatientServiceClient;
import com.example.bookingservice.dto.CreateTicketRequest;
import com.example.bookingservice.entity.Ticket;
import java.util.List;
import com.example.bookingservice.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer orchestrating the booking lifecycle.
 * Uses constructor injection and delegates state transitions to the Ticket entity.
 */
@Service
@Transactional
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final TicketRepository ticketRepository;
    private final PatientServiceClient patientServiceClient;
    private final DoctorServiceClient doctorServiceClient;

    public BookingService(TicketRepository ticketRepository,
                          PatientServiceClient patientServiceClient,
                          DoctorServiceClient doctorServiceClient) {
        this.ticketRepository = ticketRepository;
        this.patientServiceClient = patientServiceClient;
        this.doctorServiceClient = doctorServiceClient;
    }

    /**
     * Create a new booking ticket.
     * 1. Verify patient exists via Patient Service
     * 2. Check doctor availability via (mock) Doctor Service
     * 3. Persist ticket in NEW state
     */
    public Ticket createTicket(CreateTicketRequest request) {
        log.info("Creating ticket for patientId={}, doctorId={}", request.getPatientId(), request.getDoctorId());

        // Step 1: Verify patient
        boolean patientExists = patientServiceClient.verifyPatient(request.getPatientId());
        if (!patientExists) {
            throw new IllegalArgumentException("Patient not found with ID: " + request.getPatientId());
        }

        // Step 2: Check doctor availability
        boolean doctorAvailable = doctorServiceClient.checkDoctorAvailable(
                request.getDoctorId(), request.getTimeSlotId());
        if (!doctorAvailable) {
            throw new IllegalArgumentException(
                    "Doctor " + request.getDoctorId() + " is not available for time slot " + request.getTimeSlotId());
        }

        // Step 3: Create and save ticket (starts in NEW state)
        Ticket ticket = new Ticket(
                request.getPatientId(),
                request.getDoctorId(),
                request.getName(),
                request.getPhone(),
                request.getDescription(),
                request.getDate(),
                request.getTimeSlotId()
        );

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Ticket created with id={}, state={}", savedTicket.getId(), savedTicket.getStateName());
        return savedTicket;
    }

    /**
     * Confirm a ticket (NEW → PENDING).
     */
    public Ticket confirmTicket(Long id) {
        Ticket ticket = findTicketById(id);
        log.info("Confirming ticket id={}, currentState={}", id, ticket.getStateName());
        ticket.requestConfirm();
        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Ticket confirmed id={}, newState={}", savedTicket.getId(), savedTicket.getStateName());
        return savedTicket;
    }

    /**
     * Approve a ticket (PENDING → APPROVED).
     */
    public Ticket approveTicket(Long id) {
        Ticket ticket = findTicketById(id);
        log.info("Approving ticket id={}, currentState={}", id, ticket.getStateName());
        ticket.approve();
        return ticketRepository.save(ticket);
    }

    /**
     * Reject a ticket (PENDING → REJECTED).
     */
    public Ticket rejectTicket(Long id) {
        Ticket ticket = findTicketById(id);
        log.info("Rejecting ticket id={}, currentState={}", id, ticket.getStateName());
        ticket.reject();
        return ticketRepository.save(ticket);
    }

    /**
     * Cancel a ticket (NEW → REJECTED).
     */
    public Ticket cancelTicket(Long id) {
        Ticket ticket = findTicketById(id);
        log.info("Cancelling ticket id={}, currentState={}", id, ticket.getStateName());
        ticket.cancel();
        return ticketRepository.save(ticket);
    }

    /**
     * Get a ticket by ID (read-only).
     */
    @Transactional(readOnly = true)
    public Ticket getTicket(Long id) {
        return findTicketById(id);
    }

    /**
     * Get all tickets for a specific patient.
     */
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByPatient(Long patientId) {
        return ticketRepository.findByPatientIdOrderByDateDesc(patientId);
    }

    /**
     * Update a ticket (only allowed in NEW state).
     */
    public Ticket updateTicket(Long id, CreateTicketRequest request) {
        Ticket ticket = findTicketById(id);

        if (!"NEW".equals(ticket.getStateName())) {
            throw new IllegalStateException(
                    "Chỉ có thể sửa ticket ở trạng thái NEW. Trạng thái hiện tại: " + ticket.getStateName());
        }

        // Re-check doctor availability if doctor/timeslot changed
        if (!ticket.getDoctorId().equals(request.getDoctorId())
                || !ticket.getTimeSlotId().equals(request.getTimeSlotId())) {
            boolean available = doctorServiceClient.checkDoctorAvailable(
                    request.getDoctorId(), request.getTimeSlotId());
            if (!available) {
                throw new IllegalArgumentException(
                        "Doctor " + request.getDoctorId() + " is not available for time slot " + request.getTimeSlotId());
            }
        }

        ticket.setDoctorId(request.getDoctorId());
        ticket.setName(request.getName());
        ticket.setPhone(request.getPhone());
        ticket.setDescription(request.getDescription());
        ticket.setDate(request.getDate());
        ticket.setTimeSlotId(request.getTimeSlotId());

        log.info("Updated ticket id={}", id);
        return ticketRepository.save(ticket);
    }

    // =============================================
    // Private helpers
    // =============================================

    private Ticket findTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + id));
    }
}
