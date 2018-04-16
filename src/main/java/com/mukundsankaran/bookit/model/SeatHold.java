package com.mukundsankaran.bookit.model;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mukund on 4/11/18.
 *
 * SeatHold Model
 */
public class SeatHold implements Serializable {

    private static final AtomicInteger ID_GEN = new AtomicInteger();

    /**
     * SeatHold ID - Unique for each SeatHold
     */
    @NotNull
    private Integer id;

    /**
     * Event ID of the event for which the SeatHold is created
     */
    @NotNull
    private Long eventId;

    /**
     * Seat Map of the seats which make up the SeatHold
     */
    @NotNull
    private Map<Integer, List<Integer>> seats;

    /**
     * Email of the customer who requested the SeatHold
     */
    @NotNull
    @Email
    private String customerEmail;

    /**
     * The time at which the hold was made
     */
    @NotNull
    private Long holdTime;

    /**
     * Default Constructor
     */
    public SeatHold() {
        // No op
    }

    /**
     * Constructs a SeatHold
     *
     * @param seats - seats that make up the SeatHold
     * @param customerEmail - Email ID of the customer who requested the SeatHold
     * @param eventId - Event ID of the event for which the SeatHold was created
     */
    public SeatHold(Map<Integer, List<Integer>> seats, String customerEmail, Long eventId) {

        // Generate Unique ID for SeatHold
        id = ID_GEN.incrementAndGet();

        this.seats = seats;
        this.customerEmail = customerEmail;
        this.eventId = eventId;
        Instant instant = Instant.now();
        this.holdTime = instant.toEpochMilli();
    }

    public @NotNull Integer getId() {
        return id;
    }

    public void setId(@NotNull Integer id) {
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

    public @NotNull @Email String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(@NotNull @Email String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public @NotNull Long getHoldTime() {
        return holdTime;
    }

    public void setHoldTime(@NotNull Long holdTime) {
        this.holdTime = holdTime;
    }

    @Override
    public String toString() {
        return "SeatHold{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", seats=" + seats +
                ", customerEmail='" + customerEmail + '\'' +
                ", holdTime=" + holdTime +
                '}';
    }
}
