package com.mukundsankaran.bookit.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mukund on 4/13/18.
 *
 * Row Model
 */
public class Row {

    private static final AtomicInteger ID_GEN = new AtomicInteger();

    /**
     * Row ID - Unique for each Row
     */
    @NotNull
    private Integer id;

    /**
     * Number of free (unoccupied) seats in a row
     */
    @Min(0)
    private int freeSeats;

    /**
     * List of seats belonging to a row
     */
    private List<Seat> seats;

    /**
     * Default constructor
     */
    public Row(){
        // no op
    }

    /**
     * Constructs a Row
     *
     * @param seatsInRow - the number of seats in the row
     */
    public Row(int seatsInRow){

        // Generate a unique ID for the Row
        id = ID_GEN.incrementAndGet();

        List<Seat> seats = new ArrayList<>();
        int seat = 0;
        while(seat < seatsInRow){
            seats.add(new Seat());
            seat++;
        }
        this.seats = seats;
        this.freeSeats = seatsInRow;
    }

    public @NotNull Integer getId() {
        return id;
    }

    public void setId(@NotNull Integer id) {
        this.id = id;
    }

    public int getFreeSeats() {
        return freeSeats;
    }

    public void setFreeSeats(int freeSeats) {
        this.freeSeats = freeSeats;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    @Override
    public String toString() {
        return "Row{" +
                "id=" + id +
                ", freeSeats=" + freeSeats +
                ", seats=" + seats +
                '}';
    }
}
