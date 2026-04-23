package com.example.bookingservice.repository;

import com.example.bookingservice.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Ticket entities.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByPatientIdOrderByDateDesc(Long patientId);
}
