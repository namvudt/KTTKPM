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
}
