package com.example.bookingservice.entity.state;

/**
 * Terminal state: the ticket has been approved.
 * No further transitions are allowed.
 */
public class ApprovedState implements TicketState {

    @Override
    public TicketState handleConfirm() {
        throw new IllegalStateException("Ticket is already APPROVED. No further transitions allowed.");
    }

    @Override
    public TicketState handleCancel() {
        throw new IllegalStateException("Ticket is already APPROVED. No further transitions allowed.");
    }

    @Override
    public TicketState handleApprove() {
        throw new IllegalStateException("Ticket is already APPROVED. No further transitions allowed.");
    }

    @Override
    public TicketState handleReject() {
        throw new IllegalStateException("Ticket is already APPROVED. No further transitions allowed.");
    }

    @Override
    public String getName() {
        return "APPROVED";
    }
}
