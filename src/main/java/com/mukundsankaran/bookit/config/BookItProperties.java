package com.mukundsankaran.bookit.config;

import com.mukundsankaran.bookit.model.SeatingPlan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import com.mukundsankaran.bookit.validation.Enum;

/**
 * Created by mukund on 4/13/18.
 *
 * Configuration Properties for BookIt application
 */

@ConfigurationProperties(prefix = "bookit", ignoreUnknownFields = false)
@Validated
public class BookItProperties {

    public final Venue venue = new Venue();

    public Venue getVenue(){
        return venue;
    }

    public static class Venue {

        @Min(1)
        @Max(Integer.MAX_VALUE)
        private int capacity;

        @Min(1)
        @Max(Integer.MAX_VALUE)
        private int numRows;

        @Enum(enumClass = SeatingPlan.class, ignoreCase = true)
        private String seatingPlan;

        @NotBlank
        private String defaultEventName;

        @Min(1)
        @Max(30)
        private int holdExpiryTimeInMinutes;

        public @Min(1) @Max(Integer.MAX_VALUE) int getCapacity() {
            return capacity;
        }

        public void setCapacity(@Min(1) @Max(Integer.MAX_VALUE) int capacity) {
            this.capacity = capacity;
        }

        public @Min(1) @Max(Integer.MAX_VALUE) int getNumRows() {
            return numRows;
        }

        public void setNumRows(@Min(1) @Max(Integer.MAX_VALUE) int numRows) {
            this.numRows = numRows;
        }

        public String getSeatingPlan() {
            return seatingPlan;
        }

        public void setSeatingPlan(String seatingPlan) {
            this.seatingPlan = seatingPlan;
        }

        public @NotBlank String getDefaultEventName() {
            return defaultEventName;
        }

        public void setDefaultEventName(@NotBlank String defaultEventName) {
            this.defaultEventName = defaultEventName;
        }

        public @Min(1) @Max(30) int getHoldExpiryTimeInMinutes() {
            return holdExpiryTimeInMinutes;
        }

        public void setHoldExpiryTimeInMinutes(@Min(1) @Max(30) int holdExpiryTimeInMinutes) {
            this.holdExpiryTimeInMinutes = holdExpiryTimeInMinutes;
        }
    }

}
