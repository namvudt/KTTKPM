package com.example.doctorservice.repository;

import com.example.doctorservice.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    /** Find all schedules for a given time slot → get list of doctors in that slot */
    List<DoctorSchedule> findByTimeSlotId(Long timeSlotId);

    /** Find all schedules for a given doctor → get list of time slots for that doctor */
    List<DoctorSchedule> findByDoctorId(Long doctorId);

    /** Check if a specific doctor is assigned to a specific time slot */
    boolean existsByDoctorIdAndTimeSlotId(Long doctorId, Long timeSlotId);
}
