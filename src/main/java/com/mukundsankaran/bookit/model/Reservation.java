package com.mukundsankaran.bookit.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by mukund on 4/13/18.
 *
 * Reservation Model
 */
public class Reservation {

    /**
     * Reservation ID - Unique for each Reservation
     */
    @NotBlank
    private String id;

    /**
     * Event ID of the event for which the reservation is made
     */
    @NotNull
    private Long eventId;

    /**
     * Seat Map of seats that make up the reservation
     */
    @NotNull
    private Map<Integer, List<Integer>> seats;

    /**
     * Unique Email ID of customer who made the reservation
     */
    @NotBlank
    @Email
    private String customerEmail;

    /**
     * Default Constructor
     */
    public Reservation() {
        // No op
    }

    /**
     * Constructs a Reservation
     *
     * @param seats - Seat Map of seats that make up the reservation
     * @param customerEmail - Email ID of customer
     * @param eventId - Event ID of event
     */
    public Reservation(Map<Integer, List<Integer>> seats, String customerEmail, Long eventId) {

        // Generate a unique Reservation ID
        id = UUID.randomUUID().toString();

        this.seats = seats;
        this.customerEmail = customerEmail;
        this.eventId = eventId;
    }

    public @NotBlank String getId() {
        return id;
    }

    public void setId(@NotBlank String id) {
        this.id = id;
    }

    public @NotNull Long getEventId() {
        return eventId;
    }

    public void setEventId(@NotNull Long eventId) {
        this.eventId = eventId;
    }

    public @NotNull Map<Integer, List<Integer>> getSeats() {
        return seats;
    }

    public void setSeats(@NotNull Map<Integer, List<Integer>> seats) {
        this.seats = seats;
    }

    public @NotBlank @Email String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(@NotBlank @Email String customerEmail) {
        this.customerEmail = customerEmail;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id='" + id + '\'' +
                ", eventId=" + eventId +
                ", seats=" + seats +
                ", customerEmail='" + customerEmail + '\'' +
                '}';
    }
}
