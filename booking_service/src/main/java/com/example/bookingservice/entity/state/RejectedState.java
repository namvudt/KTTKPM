package com.example.bookingservice.entity.state;

/**
 * Terminal state: the ticket has been rejected or cancelled.
 * No further transitions are allowed.
 */
public class RejectedState implements TicketState {

    @Override
    public TicketState handleConfirm() {
        throw new IllegalStateException("Ticket is REJECTED. No further transitions allowed.");
    }

    @Override
    public TicketState handleCancel() {
        throw new IllegalStateException("Ticket is already REJECTED. No further transitions allowed.");
    }

    @Override
    public TicketState handleApprove() {
        throw new IllegalStateException("Ticket is REJECTED. No further transitions allowed.");
    }

    @Override
    public TicketState handleReject() {
        throw new IllegalStateException("Ticket is already REJECTED. No further transitions allowed.");
    }

    @Override
    public String getName() {
        return "REJECTED";
    }
}
