package com.example.bookingservice.entity.state;

/**
 * Initial state of a newly created ticket.
 * Allowed transitions:
 *   confirm → PendingState
 *   cancel  → RejectedState
 */
public class NewState implements TicketState {

    @Override
    public TicketState handleConfirm() {
        return new PendingState();
    }

    @Override
    public TicketState handleCancel() {
        return new RejectedState();
    }

    @Override
    public TicketState handleApprove() {
        throw new IllegalStateException("Cannot approve a ticket in NEW state. Must confirm first.");
    }

    @Override
    public TicketState handleReject() {
        throw new IllegalStateException("Cannot reject a ticket in NEW state. Must confirm first.");
    }

    @Override
    public String getName() {
        return "NEW";
    }
}
