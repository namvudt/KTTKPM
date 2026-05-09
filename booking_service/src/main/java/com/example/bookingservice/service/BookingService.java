package com.example.bookingservice.service;

import com.example.bookingservice.client.DoctorServiceClient;
import com.example.bookingservice.client.PatientServiceClient;
import com.example.bookingservice.dto.CreateTicketRequest;
import com.example.bookingservice.entity.Ticket;
import java.util.List;
import java.util.Map;
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

        // Step 1: Validate booking date is not in the past
        validateBookingDate(request.getDate());

        // Step 2: Verify patient
        boolean patientExists = patientServiceClient.verifyPatient(request.getPatientId());
        if (!patientExists) {
            throw new IllegalArgumentException("Patient not found with ID: " + request.getPatientId());
        }

        // Step 3: Check doctor availability (is assigned to this time slot)
        boolean doctorAvailable = doctorServiceClient.checkDoctorAvailable(
                request.getDoctorId(), request.getTimeSlotId());
        if (!doctorAvailable) {
            throw new IllegalArgumentException(
                    "Doctor " + request.getDoctorId() + " is not available for time slot " + request.getTimeSlotId());
        }

        // Step 4: Check patient doesn't already have an active ticket on this date+slot
        checkNoPatientConflict(request.getPatientId(), request.getTimeSlotId(), request.getDate(), null);

        // Step 5: Check no APPROVED ticket already exists for this doctor+timeslot+date
        checkNoApprovedConflict(request.getDoctorId(), request.getTimeSlotId(), request.getDate(), null);

        // Step 6: Create and save ticket (starts in NEW state)
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
     * Return the IDs of doctors who have reached their maximum patient capacity
     * for the given time slot and date.
     * Used by frontend to filter the doctor list — only available doctors are shown.
     */
    @Transactional(readOnly = true)
    public List<Long> getBookedDoctorIds(Long timeSlotId, java.time.LocalDate date) {
        int maxPatients = doctorServiceClient.getTimeSlotMaxPatients(timeSlotId);

        // Group active (non-rejected) tickets by doctorId and find those at capacity
        Map<Long, Long> countByDoctor = ticketRepository.findByTimeSlotIdAndDate(timeSlotId, date)
                .stream()
                .filter(t -> "PENDING".equals(t.getStateName()) || "APPROVED".equals(t.getStateName()))
                .collect(java.util.stream.Collectors.groupingBy(
                        Ticket::getDoctorId, java.util.stream.Collectors.counting()));

        return countByDoctor.entrySet().stream()
                .filter(e -> e.getValue() >= maxPatients)
                .map(java.util.Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
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

        // Always validate the new date
        validateBookingDate(request.getDate());

        // Re-check availability and conflicts if doctor/timeslot/date changed
        boolean doctorOrSlotChanged = !ticket.getDoctorId().equals(request.getDoctorId())
                || !ticket.getTimeSlotId().equals(request.getTimeSlotId());
        boolean dateChanged = !ticket.getDate().equals(request.getDate());

        if (doctorOrSlotChanged || dateChanged) {
            boolean available = doctorServiceClient.checkDoctorAvailable(
                    request.getDoctorId(), request.getTimeSlotId());
            if (!available) {
                throw new IllegalArgumentException(
                        "Doctor " + request.getDoctorId() + " is not available for time slot " + request.getTimeSlotId());
            }
            // Exclude current ticket from both conflict checks
            checkNoPatientConflict(ticket.getPatientId(), request.getTimeSlotId(), request.getDate(), id);
            checkNoApprovedConflict(request.getDoctorId(), request.getTimeSlotId(), request.getDate(), id);
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

    /**
     * Throws an exception if the booking date is in the past.
     */
    private void validateBookingDate(java.time.LocalDate date) {
        if (date.isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Ngày khám không hợp lệ: " + date + " đã là ngày trong quá khứ. Vui lòng chọn ngày từ hôm nay trở đi.");
        }
    }

    /**
     * Throws an exception if the patient already has an ACTIVE ticket
     * (NEW / PENDING / APPROVED) for the same time slot and date.
     *
     * @param excludeTicketId optional: ticket ID to exclude from the check (used during update)
     */
    private void checkNoPatientConflict(Long patientId, Long timeSlotId,
                                         java.time.LocalDate date, Long excludeTicketId) {
        List<Ticket> existing = ticketRepository
                .findByPatientIdAndTimeSlotIdAndDate(patientId, timeSlotId, date);

        boolean hasActive = existing.stream()
                .filter(t -> excludeTicketId == null || !t.getId().equals(excludeTicketId))
                .anyMatch(t -> !"REJECTED".equals(t.getStateName()));

        if (hasActive) {
            log.warn("Patient conflict blocked: patientId={}, timeSlotId={}, date={}", patientId, timeSlotId, date);
            throw new IllegalArgumentException(
                    "Bạn đã có lịch khám trong ca này ngày " + date +
                    ". Không thể đặt thêm lịch cùng khung giờ.");
        }
    }

    /**
     * Throws an exception if the doctor has reached their maximum patient capacity
     * (maxPatients active tickets) for the given time slot and date.
     * Replaces the old single-APPROVED-ticket check.
     *
     * @param excludeTicketId optional: ticket ID to exclude from the check (used during update)
     */
    private void checkNoApprovedConflict(Long doctorId, Long timeSlotId,
                                          java.time.LocalDate date, Long excludeTicketId) {
        int maxPatients = doctorServiceClient.getTimeSlotMaxPatients(timeSlotId);

        long activeCount = ticketRepository
                .findByDoctorIdAndTimeSlotIdAndDate(doctorId, timeSlotId, date)
                .stream()
                .filter(t -> excludeTicketId == null || !t.getId().equals(excludeTicketId))
                .filter(t -> "PENDING".equals(t.getStateName()) || "APPROVED".equals(t.getStateName()))
                .count();

        if (activeCount >= maxPatients) {
            log.warn("Capacity reached: doctorId={}, timeSlotId={}, date={}, activeCount={}, maxPatients={}",
                    doctorId, timeSlotId, date, activeCount, maxPatients);
            throw new IllegalArgumentException(
                    "Bác sĩ này đã đủ " + maxPatients + " bệnh nhân trong ca này ngày " + date +
                    ". Vui lòng chọn bác sĩ hoặc ngày khác.");
        }
    }
}
