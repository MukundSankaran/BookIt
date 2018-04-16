package com.mukundsankaran.bookit.model;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mukund on 4/15/18.
 *
 * Seat Model
 */
public class Seat implements Serializable {

    private static final AtomicInteger ID_GEN = new AtomicInteger();

    /**
     * Seat ID - Unique for each Seat
     */
    @NotNull
    private Integer id;

    /**
     * Boolean property that indicates whether the seat is occupied
     */
    @NotNull
    private Boolean empty;

    /**
     * Constructs a Seat
     */
    public Seat() {
        // Generate a unique ID for the Seat
        id = ID_GEN.incrementAndGet();
        empty = true;
    }

    public @NotNull Integer getId() {
        return id;
    }

    public void setId(@NotNull Integer id) {
        this.id = id;
    }

    public @NotNull Boolean getEmpty() {
        return empty;
    }

    public void setEmpty(@NotNull Boolean empty) {
        this.empty = empty;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "id=" + id +
                ", empty=" + empty +
                '}';
    }
}
