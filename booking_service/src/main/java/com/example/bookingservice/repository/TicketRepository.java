package com.example.bookingservice.repository;

import com.example.bookingservice.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for Ticket entities.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByPatientIdOrderByDateDesc(Long patientId);

    /**
     * Find all tickets for a specific doctor, time slot, and date.
     * Used to check for double-booking before creating or updating a ticket.
     */
    List<Ticket> findByDoctorIdAndTimeSlotIdAndDate(Long doctorId, Long timeSlotId, LocalDate date);

    /**
     * Find all tickets for a given time slot and date (across all doctors).
     * Used to determine which doctors are already fully booked on that slot+date.
     */
    List<Ticket> findByTimeSlotIdAndDate(Long timeSlotId, LocalDate date);

    /**
     * Find all tickets for a specific patient, time slot, and date.
     * Used to prevent a patient from booking the same slot twice on the same day.
     */
    List<Ticket> findByPatientIdAndTimeSlotIdAndDate(Long patientId, Long timeSlotId, LocalDate date);
}

