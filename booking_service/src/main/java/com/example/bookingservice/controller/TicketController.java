package com.example.bookingservice.controller;

import com.example.bookingservice.dto.CreateTicketRequest;
import com.example.bookingservice.dto.TicketResponse;
import com.example.bookingservice.entity.Ticket;
import java.util.List;
import java.util.stream.Collectors;
import com.example.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing the Booking ticket lifecycle API.
 */
@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final BookingService bookingService;

    public TicketController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Create a new booking ticket.
     * POST /tickets
     */
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        Ticket ticket = bookingService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketResponse.fromEntity(ticket));
    }

    /**
     * Confirm a ticket (NEW → PENDING).
     * POST /tickets/{id}/confirm
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<TicketResponse> confirmTicket(@PathVariable Long id) {
        Ticket ticket = bookingService.confirmTicket(id);
        return ResponseEntity.ok(TicketResponse.fromEntity(ticket));
    }

    /**
     * Approve a ticket (PENDING → APPROVED).
     * POST /tickets/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<TicketResponse> approveTicket(@PathVariable Long id) {
        Ticket ticket = bookingService.approveTicket(id);
        return ResponseEntity.ok(TicketResponse.fromEntity(ticket));
    }

    /**
     * Reject a ticket (PENDING → REJECTED).
     * POST /tickets/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<TicketResponse> rejectTicket(@PathVariable Long id) {
        Ticket ticket = bookingService.rejectTicket(id);
        return ResponseEntity.ok(TicketResponse.fromEntity(ticket));
    }

    /**
     * Cancel a ticket (NEW → REJECTED).
     * POST /tickets/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TicketResponse> cancelTicket(@PathVariable Long id) {
        Ticket ticket = bookingService.cancelTicket(id);
        return ResponseEntity.ok(TicketResponse.fromEntity(ticket));
    }

    /**
     * Get ticket details.
     * GET /tickets/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Long id) {
        Ticket ticket = bookingService.getTicket(id);
        return ResponseEntity.ok(TicketResponse.fromEntity(ticket));
    }

    /**
     * Get all tickets for a patient.
     * GET /tickets?patientId=1
     */
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getTicketsByPatient(@RequestParam Long patientId) {
        List<TicketResponse> tickets = bookingService.getTicketsByPatient(patientId)
                .stream()
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tickets);
    }

    /**
     * Update a ticket (only allowed in NEW state).
     * PUT /tickets/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long id, @Valid @RequestBody CreateTicketRequest request) {
        Ticket ticket = bookingService.updateTicket(id, request);
        return ResponseEntity.ok(TicketResponse.fromEntity(ticket));
    }
}
