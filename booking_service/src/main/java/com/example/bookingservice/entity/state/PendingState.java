package com.example.bookingservice.entity.state;

/**
 * State after a ticket has been confirmed by the patient.
 * Allowed transitions:
 *   approve → ApprovedState
 *   reject  → RejectedState
 */
public class PendingState implements TicketState {

    @Override
    public TicketState handleConfirm() {
        throw new IllegalStateException("Ticket is already confirmed and in PENDING state.");
    }

    @Override
    public TicketState handleCancel() {
        throw new IllegalStateException("Cannot cancel a ticket in PENDING state. Use reject instead.");
    }

    @Override
    public TicketState handleApprove() {
        return new ApprovedState();
    }

    @Override
    public TicketState handleReject() {
        return new RejectedState();
    }

    @Override
    public String getName() {
        return "PENDING";
    }
}
