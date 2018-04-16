package com.mukundsankaran.bookit.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by mukund on 4/11/18.
 *
 * Event Model
 */
public class Event implements Serializable {

    private static final AtomicLong ID_GEN = new AtomicLong();

    /**
     * Event ID - Unique for each Event
     */
    @NotNull
    private Long id;

    /**
     * Event Name
     */
    @NotBlank
    private String name;

    /**
     * Number of Seats Available for Event
     */
    @Min(0)
    private int numSeatsAvailable;

    /**
     * Default Constructor
     */
    private Event(){
        // No op
    }

    /**
     * Constructs an Event
     *
     * @param name - Event Name
     * @param numSeatsAvailable - Seats Available for Event
     */
    public Event(String name, int numSeatsAvailable){

        // Generate Unique ID for Event
        id = ID_GEN.incrementAndGet();

        this.name = name;
        this.numSeatsAvailable = numSeatsAvailable;
    }

    public @NotNull Long getId() {
        return id;
    }

    public void setId(@NotNull Long id) {
        this.id = id;
    }

    public @NotBlank String getName() {
        return name;
    }

    public void setName(@NotBlank String name) {
        this.name = name;
    }

    public @Min(0) int getNumSeatsAvailable() {
        return numSeatsAvailable;
    }

    public void setNumSeatsAvailable(@Min(0) int numSeatsAvailable) {
        this.numSeatsAvailable = numSeatsAvailable;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", numSeatsAvailable=" + numSeatsAvailable +
                '}';
    }
}
