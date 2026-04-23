package com.example.bookingservice.entity.state;

/**
 * State Pattern interface for Ticket lifecycle management.
 * Each concrete state defines which transitions are valid.
 */
public interface TicketState {

    /**
     * Handle confirm action on the ticket.
     * @return the next state after confirmation
     */
    TicketState handleConfirm();

    /**
     * Handle cancel action on the ticket.
     * @return the next state after cancellation
     */
    TicketState handleCancel();

    /**
     * Handle approve action on the ticket.
     * @return the next state after approval
     */
    TicketState handleApprove();

    /**
     * Handle reject action on the ticket.
     * @return the next state after rejection
     */
    TicketState handleReject();

    /**
     * @return the name of this state for persistence
     */
    String getName();
}
