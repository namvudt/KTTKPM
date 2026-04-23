package com.example.bookingservice.entity.state;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter to persist TicketState as a simple String column
 * in the database and reconstruct the correct State object when reading.
 */
@Converter(autoApply = true)
public class TicketStateConverter implements AttributeConverter<TicketState, String> {

    @Override
    public String convertToDatabaseColumn(TicketState state) {
        if (state == null) {
            return null;
        }
        return state.getName();
    }

    @Override
    public TicketState convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return switch (dbData) {
            case "NEW" -> new NewState();
            case "PENDING" -> new PendingState();
            case "APPROVED" -> new ApprovedState();
            case "REJECTED" -> new RejectedState();
            default -> throw new IllegalArgumentException("Unknown ticket state: " + dbData);
        };
    }
}
