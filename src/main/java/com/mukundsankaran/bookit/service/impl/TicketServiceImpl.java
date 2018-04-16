package com.mukundsankaran.bookit.service.impl;

import com.mukundsankaran.bookit.config.BookItProperties;
import com.mukundsankaran.bookit.model.*;
import com.mukundsankaran.bookit.service.TicketService;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mukund on 4/11/18.
 *
 * BookIt Ticket Service Implementation
 *
 */

@Service
public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    @Autowired
    private Ignite ignite;

    @Autowired
    private BookItProperties bookItProperties;

    private long holdExpiryTime;

    /**
     * Initialize TicketService
     */
    @PostConstruct
    private void init() {
        // convert into milliseconds
        holdExpiryTime = bookItProperties.getVenue().getHoldExpiryTimeInMinutes() * 60000;
    }

    /**
     * The number of seats in the venue that are neither held nor reserved
     *
     * @return the number of tickets available in the venue
     */
    public int numSeatsAvailable() {

        if(logger.isDebugEnabled()) {
            logger.debug("Finding number of available seats.");
        }

        IgniteCache<Long, Event> eventCache = ignite.cache(CacheName.EVENTS.name());
        final int numSeatsAvailable = eventCache.get(eventCache.iterator().next().getKey()).getNumSeatsAvailable();

        if(logger.isDebugEnabled()) {
            logger.debug("{} seats are available.", numSeatsAvailable);
        }

        return numSeatsAvailable;
    }

    /**
     * Find and hold the best available seats for a customer
     *
     * @param numSeats the number of seats to find and hold
     * @param customerEmail unique identifier for the customer
     * @return a SeatHold object identifying the specific seats and related information
     */
    public synchronized SeatHold findAndHoldSeats(int numSeats, String customerEmail) {

        if(logger.isDebugEnabled()) {
            logger.debug("Finding best {} seats for customer {}.", numSeats, customerEmail);
        }

        try(Transaction tx = ignite.transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.REPEATABLE_READ)) {

            IgniteCache<Long, Event> eventCache = ignite.cache(CacheName.EVENTS.name());
            Event event = eventCache.get(eventCache.iterator().next().getKey());

            if(event != null && isSeatHoldRequestValid(event, numSeats, customerEmail)){

                if(logger.isDebugEnabled()) {
                    logger.debug("Retrieved event {}.", event);
                }

                // Find Best Seats
                Map<Integer, List<Integer>> seatMap = new HashMap<>();

                // Attempt to assign seats contiguously
                boolean seatsAssigned = assignSeatsContiguously(numSeats, seatMap);

                // If contiguous seats are not found, assign seats in a staggered fashion from front to rear
                if(!seatsAssigned) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("{} contiguous seats not found. Attempting staggered assignment",  numSeats);
                    }
                    assignSeatsStaggered(numSeats, seatMap);
                }

                // Create a SeatHold object and add it to the cache
                SeatHold hold = new SeatHold(seatMap, customerEmail, event.getId());
                IgniteCache<Integer, SeatHold> holdCache = ignite.cache(CacheName.HOLDS.name());
                holdCache.put(hold.getId(), hold);

                if(logger.isDebugEnabled()) {
                    logger.debug("SeatHold {} created successfully.", hold);
                }

                // Update the event with the number of seats available after placing the hold
                event.setNumSeatsAvailable(event.getNumSeatsAvailable() - numSeats);
                eventCache.replace(event.getId(), event);

                if(logger.isDebugEnabled()) {
                    logger.debug("Number of available seats in Event {} updated.", event);
                }

                tx.commit();

                return hold;
            }

        }

        return null;
    }

    /**
     * Check if the request to hold seats is valid
     *
     * @param event - the event
     * @param customerEmail - the Email of the customer who made the SeatHold request
     * @param numSeats - number of seats requested by the customer
     * @return a boolean indicating whether the request is valid
     */
    private boolean isSeatHoldRequestValid(Event event, int numSeats, String customerEmail){

        if(logger.isDebugEnabled()) {
            logger.debug("Checking if request from customer {} is valid.", customerEmail);
        }

        // Check if the number of requested seats is greater than 0
        if(numSeats <= 0){
            if(logger.isDebugEnabled()) {
                logger.debug("Unable to service request from customer {}. Requested number of seats need to be greater than 0.", customerEmail);
            }
            return false;
        }

        // Check if there are enough available seats to service the request
        int numSeatsAvailable = event.getNumSeatsAvailable();
        if(numSeats > numSeatsAvailable) {
            if(logger.isDebugEnabled()) {
                logger.debug("Unable to service request from customer {}. Requested number of seats unavailable.", customerEmail);
            }
            return false;
        }

        // Check if there is an existing hold for the customer and the event - if yes, reject the request
        IgniteCache<Integer, SeatHold> holdCache = ignite.cache(CacheName.HOLDS.name());
        List<Integer> holds = holdCache.query(
            new ScanQuery<Integer, SeatHold>(
                (k, v) -> v.getCustomerEmail().equalsIgnoreCase(customerEmail) && v.getEventId().equals(event.getId())
            ),
            Cache.Entry::getKey
        ).getAll();

        if(holds.size() > 0) {
            if(logger.isDebugEnabled()) {
                logger.debug("Unable to service request from customer {}. A hold already exists with ID {} for event {}.",customerEmail, holds.get(0), event.getName());
            }
            return false;
        }

        // Check if there is an existing reservation for the customer and the event - if yes, reject the request
        IgniteCache<String, Reservation> reservationCache = ignite.cache(CacheName.RESERVATIONS.name());
        List<String> reservations = reservationCache.query(
            new ScanQuery<String, Reservation>(
                (k, v) -> v.getCustomerEmail().equalsIgnoreCase(customerEmail) && v.getEventId().equals(event.getId())
            ),
            Cache.Entry::getKey
        ).getAll();

        if(reservations.size() > 0){
            if(logger.isDebugEnabled()){
                logger.debug("Unable to service request from customer {}. A reservation already exists with ID {} for event {}.",customerEmail, reservations.get(0), event.getName());
            }
            return false;
        }

        return true;
    }

    /**
     * A helper method that attempts to assign seats contiguously if possible
     *
     * @param numSeats - number of seats requested by the customer
     * @param seatMap - the seat map for the assignment
     * @return - a boolean indicating whether seat assignment was successful
     */
    private boolean assignSeatsContiguously(int numSeats, Map<Integer, List<Integer>> seatMap){

        boolean seatsAssigned = false;

        IgniteCache<Integer, Row> rowCache= ignite.cache(CacheName.ROWS.name());

        if(logger.isDebugEnabled()) {
            logger.debug("Attempting to assign {} seats contiguously.", numSeats);
        }

        Iterator<Cache.Entry<Integer, Row>> iterator = rowCache.iterator();

        while(iterator.hasNext() && !seatsAssigned) {

            int rowNum = iterator.next().getKey();

            // Get the row
            Row row = rowCache.get(rowNum);

            // Check if the row contains any free seats
            if(row.getFreeSeats() > 0){

                // Fetch list of unoccupied seats
                List<Seat> emptySeats = getEmptySeats(row);

                // If "numSeats" contiguous seats are available in the row, return the index of the first seat in the contiguous list
                int start = findSequenceStart(emptySeats, numSeats);

                if(start >= 0) {

                    if(logger.isDebugEnabled()) {
                        logger.debug("Row {} has {} contiguous seats.", rowNum, numSeats);
                    }

                    seatMap.put(row.getId(), new ArrayList<>());

                    int seatId = start;

                    for(int i = 0; i < numSeats; i++) {
                        fillSeat(emptySeats, seatId);
                        seatMap.get(row.getId()).add(seatId);
                        seatId++;
                    }

                    seatsAssigned = true;

                    row.setFreeSeats(emptySeats.size() - numSeats);
                    rowCache.put(row.getId(), row);
                }
            }
        }

        return seatsAssigned;
    }

    /**
     * A helper method that attempts to assign seats from front to back
     *
     * @param numSeats - number of seats requested by the customer
     * @param seatMap - the seat map for the assignment
     * @return - a boolean indicating whether seat assignment was successful
     */
    private boolean assignSeatsStaggered(int numSeats, Map<Integer, List<Integer>> seatMap) {

        boolean seatsAssigned = false;

        IgniteCache<Integer, Row> rowCache= ignite.cache(CacheName.ROWS.name());

        if(logger.isDebugEnabled()) {
            logger.debug("Attempting to assign {} seats in staggered fashion.", numSeats);
        }

        int assignedSeatCount = 0;
        Iterator<Cache.Entry<Integer, Row>> iterator = rowCache.iterator();
        while(iterator.hasNext() && !seatsAssigned) {

            int rowNum = iterator.next().getKey();

            // Get the row
            Row row = rowCache.get(rowNum);

            // Check if the row contains any free seats
            if(row.getFreeSeats() > 0) {

                // Fetch list of unoccupied seats
                List<Seat> emptySeats = getEmptySeats(row);

                seatMap.put(row.getId(), new ArrayList<>());

                for(Seat seat : emptySeats){

                    seat.setEmpty(false);
                    row.setFreeSeats(row.getFreeSeats() - 1);
                    rowCache.put(row.getId(), row);

                    seatMap.get(row.getId()).add(seat.getId());

                    assignedSeatCount++;
                    if(assignedSeatCount == numSeats){
                        seatsAssigned = true;
                        if(logger.isDebugEnabled()) {
                            logger.debug("Staggered assignment of {} seats successful.", numSeats);
                        }
                        break;
                    }
                }
            }
        }

        return seatsAssigned;
    }

    /**
     * Get the list of empty seats in a row
     *
     * @param row - the row for which the list of empty seats needs to be retrieved
     * @return - the list of empty seats in the row
     */
    private List<Seat> getEmptySeats(Row row){
        return row.getSeats().stream()
                    .filter(Seat::getEmpty)
                    .collect(Collectors.toList());
    }

    /**
     * Helper method for filling a contiguous seat
     *
     * @param seats - the list of empty seats in the row
     * @param seatId - the ID of the seat that needs to be marked as occupied
     */
    private void fillSeat(List<Seat> seats, int seatId) {

        if(logger.isDebugEnabled()) {
            logger.debug("Filling Contiguous Seat: " + seatId);
        }

        seats.stream().filter(s -> s.getId() == seatId)
            .findAny()
            .ifPresent(s -> s.setEmpty(false));
    }

    /**
     * If "numSeats" contiguous seats are available in the row, find the index of the first seat in the contiguous list
     *
     * @param emptySeats - the list of empty seats in the row
     * @param sequenceLength - the number of contiguous seats requested
     * @return - the index of the first seat in the contiguous list, if found, and -1 otherwise
     */
    private int findSequenceStart(List<Seat> emptySeats, int sequenceLength) {

        int[] seatsArray = emptySeats.stream()
                .mapToInt(Seat::getId)
                .toArray();

        int start = seatsArray[0];
        int count = 0;

        for(int i = 1; i < seatsArray.length; i++) {
            if(seatsArray[i] == seatsArray[i-1] + 1) {
                count++;
            } else {
                start = seatsArray[i];
                count = 0;
            }
            if(count == sequenceLength) {
                return start;
            }
        }

        return -1;
    }

    /**
     * Commit seats held for a specific customer
     *
     * @param seatHoldId the seat hold identifier
     * @param customerEmail the email address of the customer to which the seat hold is assigned
     * @return a reservation confirmation code
     */
    public String reserveSeats(int seatHoldId, String customerEmail) {

        if(logger.isDebugEnabled()) {
            logger.debug("Attempting to reserve hold with ID {} for customer {}.", seatHoldId, customerEmail);
        }

        try(Transaction tx = ignite.transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.REPEATABLE_READ)) {

            IgniteCache<Integer, SeatHold> holdCache = ignite.cache(CacheName.HOLDS.name());
            IgniteCache<String, Reservation> reservationCache = ignite.cache(CacheName.RESERVATIONS.name());
            IgniteCache<Long, Event> eventCache = ignite.cache(CacheName.EVENTS.name());

            Event event = eventCache.get(eventCache.iterator().next().getKey());

            SeatHold hold = holdCache.get(seatHoldId);

            if(hold == null){

                if(logger.isDebugEnabled()) {
                    logger.debug("No hold with ID {} found for customer {}. Checking with Email ID", seatHoldId, customerEmail);
                }

                List<Integer> holds = holdCache.query(
                        new ScanQuery<Integer, SeatHold>(
                                (k, v) -> v.getCustomerEmail().equalsIgnoreCase(customerEmail) && v.getEventId().equals(event.getId())
                        ),
                        Cache.Entry::getKey
                ).getAll();

                if(holds.size() > 0) {
                    seatHoldId = holds.get(0);
                    hold = holdCache.get(seatHoldId);

                    if(logger.isDebugEnabled()) {
                        logger.debug("Found hold {} for customer {}", hold, customerEmail);
                    }

                } else {
                    logger.debug("No hold found for seatHoldId {} or customer Email {}", seatHoldId, customerEmail);
                    return null;
                }
            }

            if(holdCache.remove(seatHoldId)){

                if(logger.isDebugEnabled()) {
                    logger.debug("Hold {} successfully removed from cache. Adding to reservation cache.", hold);
                }

                Reservation reservation = new Reservation(hold.getSeats(), hold.getCustomerEmail(), event.getId());
                reservationCache.put(reservation.getId(), reservation);

                tx.commit();

                return reservation.getId();
            }

            return null;
        }
    }

    /**
     * A scheduled task that checks for and cleans up expired holds every minute
     */
    @Scheduled(fixedDelay = 60000)
    private void expireHolds(){

        if(logger.isDebugEnabled()) {
            logger.debug("Checking for expired holds.");
        }

        try(Transaction tx = ignite.transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.REPEATABLE_READ)) {

            IgniteCache<Long, Event> eventCache = ignite.cache(CacheName.EVENTS.name());
            if(eventCache.iterator().hasNext()) {
                Event event = eventCache.get(eventCache.iterator().next().getKey());
                IgniteCache<Integer, Row> rowCache= ignite.cache(CacheName.ROWS.name());
                IgniteCache<Integer, SeatHold> holdCache = ignite.cache(CacheName.HOLDS.name());

                Instant instant = Instant.now();
                long currentTime = instant.toEpochMilli();

                List<SeatHold> holds = holdCache.query(
                        new ScanQuery<Integer, SeatHold>(
                                (k, v) -> (v.getHoldTime() + holdExpiryTime) <= currentTime
                        ),
                        Cache.Entry::getValue
                ).getAll();

                List<Map<Integer, List<Integer>>> seatMaps = new ArrayList<>();

                holds.stream()
                        .forEach(seatHold -> {
                            if(logger.isDebugEnabled()) {
                                logger.debug("Found expired hold {}.", seatHold);
                            }
                            if(holdCache.remove(seatHold.getId())){
                                if(logger.isDebugEnabled()) {
                                    logger.debug("Successfully removed expired hold {}.", seatHold);
                                }
                                seatMaps.add(seatHold.getSeats());
                            }
                        });

                seatMaps.stream()
                        .forEach(seatMap -> {
                            for(Integer rowId : seatMap.keySet()){

                                Row row = rowCache.get(rowId);
                                List<Integer> seatNumbers = seatMap.get(rowId);

                                row.getSeats().stream()
                                        .filter(s -> seatNumbers.contains(s.getId()))
                                        .forEach(s -> s.setEmpty(true));

                                row.setFreeSeats(row.getFreeSeats() + seatNumbers.size());
                                rowCache.replace(rowId, row);

                                event.setNumSeatsAvailable(event.getNumSeatsAvailable() + seatNumbers.size());

                                if(logger.isDebugEnabled()) {
                                    logger.debug("Successfully freed seats {} from row {}.", seatNumbers, rowId);
                                }
                            }
                        });

                eventCache.replace(event.getId(), event);

                tx.commit();
            }

        }
    }

}
