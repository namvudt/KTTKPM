package com.example.bookingservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * REST client for the Doctor Service.
 *
 * Calls the external Doctor Service to check doctor availability.
 * Endpoint: GET /doctors/{doctorId}/availability?timeSlotId={timeSlotId}
 *
 * If the Doctor Service is unreachable, falls back to returning true
 * (graceful degradation).
 */
@Component
public class DoctorServiceClient {

    private static final Logger log = LoggerFactory.getLogger(DoctorServiceClient.class);

    private final RestTemplate restTemplate;
    private final String doctorServiceUrl;

    public DoctorServiceClient(
            @Value("${doctor.service.url}") String doctorServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.doctorServiceUrl = doctorServiceUrl;
    }

    /**
     * Check if a doctor is available for the given time slot.
     *
     * @param doctorId   the doctor to check
     * @param timeSlotId the time slot to check availability for
     * @return true if the doctor is available
     */
    @SuppressWarnings("unchecked")
    public boolean checkDoctorAvailable(Long doctorId, Long timeSlotId) {
        try {
            String url = doctorServiceUrl + "/doctors/" + doctorId
                    + "/availability?timeSlotId=" + timeSlotId;
            log.info("Calling Doctor Service: GET {}", url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            boolean available = (boolean) response.get("available");

            log.info("Doctor {} availability for timeSlot {}: {}", doctorId, timeSlotId, available);
            return available;
        } catch (Exception e) {
            log.warn("Doctor Service call failed: {}. Falling back to available=true", e.getMessage());
            // Graceful fallback: if Doctor Service is down, assume available
            return true;
        }
    }

    /**
     * Get the maximum number of patients allowed per doctor for a given time slot.
     * Calls GET /timeslots/{id} on Doctor Service.
     *
     * @param timeSlotId the time slot ID
     * @return maxPatients value, defaults to 1 if the call fails
     */
    @SuppressWarnings("unchecked")
    public int getTimeSlotMaxPatients(Long timeSlotId) {
        try {
            String url = doctorServiceUrl + "/timeslots/" + timeSlotId;
            log.info("Calling Doctor Service for timeslot capacity: GET {}", url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            Object maxP = response.get("maxPatients");

            if (maxP instanceof Number) {
                int capacity = ((Number) maxP).intValue();
                log.info("TimeSlot {} maxPatients={}", timeSlotId, capacity);
                return capacity;
            }
            return 1;
        } catch (Exception e) {
            log.warn("Could not get maxPatients for timeSlot {}: {}. Falling back to 1", timeSlotId, e.getMessage());
            return 1; // safe fallback — treat as single-patient slot
        }
    }
}
