package com.example.bookingservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * REST client for the Patient Service.
 *
 * Calls the external Patient Service to verify that a patient exists.
 * In production, this would perform a real HTTP GET to /patients/{id}.
 * Currently includes a fallback if the Patient Service is unreachable.
 */
@Component
public class PatientServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PatientServiceClient.class);

    private final RestTemplate restTemplate;
    private final String patientServiceUrl;

    public PatientServiceClient(
            @Value("${patient.service.url}") String patientServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.patientServiceUrl = patientServiceUrl;
    }

    /**
     * Verify that a patient exists by calling GET /patients/{patientId}.
     *
     * @param patientId the patient ID to verify
     * @return true if patient exists, false otherwise
     */
    public boolean verifyPatient(Long patientId) {
        try {
            String url = patientServiceUrl + "/patients/" + patientId;
            log.info("Calling Patient Service: GET {}", url);
            restTemplate.getForObject(url, Object.class);
            log.info("Patient {} verified successfully", patientId);
            return true;
        } catch (Exception e) {
            log.warn("Patient Service call failed for patientId={}: {}", patientId, e.getMessage());
            return false;
        }
    }
}
