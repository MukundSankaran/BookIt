package com.mukundsankaran.bookit.startup;

import com.mukundsankaran.bookit.config.BookItProperties;
import com.mukundsankaran.bookit.model.CacheName;
import com.mukundsankaran.bookit.model.Event;
import com.mukundsankaran.bookit.model.Row;
import com.mukundsankaran.bookit.model.SeatingPlan;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Created by mukund on 4/12/18.
 *
 * BookIt Application Startup Runner
 */

@Component
@EnableConfigurationProperties(BookItProperties.class)
public class ApplicationStartupRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupRunner.class);

    @Autowired
    private Ignite ignite;

    @Autowired
    private BookItProperties bookItProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        try(Transaction tx = ignite.transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.REPEATABLE_READ)) {

            // Fetch configuration properties
            final String eventName = bookItProperties.getVenue().getDefaultEventName();
            final int numRows = bookItProperties.getVenue().getNumRows();
            final int numSeats = bookItProperties.getVenue().getCapacity();
            final String seatingPlan = bookItProperties.getVenue().getSeatingPlan();

            // Create default event & add to cache
            createEvent(eventName, numSeats);

            // Create rows based on configured seating plan & add to cache
            createRows(numRows, numSeats, seatingPlan);

            if(logger.isDebugEnabled()){
                logger.debug("Initial Data Loaded.");
            }

            tx.commit();
        }
    }

    /**
     * Create Event and add it to EVENTS cache
     *
     * @param eventName - name of the event
     * @param numSeats - number of seats at the venue
     */
    private void createEvent(String eventName, int numSeats){

        Event event = new Event(eventName, numSeats);
        IgniteCache<Long, Event> eventCache = ignite.cache(CacheName.EVENTS.name());
        eventCache.put(event.getId(), event);

        if(logger.isDebugEnabled()){
            logger.debug("Created Default Event: " + event);
        }

    }

    /**
     * Create Rows based on configured seating plan and add to ROWS cache
     *
     * @param numRows - number of rows at the venue
     * @param numSeats - number of seats at the venue
     * @param seatingPlan - seating plan used at the venue
     */
    private void createRows(int numRows, int numSeats, String seatingPlan){

        int remainingRows = numRows;
        int remainingSeats = numSeats;
        int seatsInRow;
        Random random = new Random();

        IgniteCache<Integer, Row> rowCache = ignite.cache(CacheName.ROWS.name());

        while(remainingRows > 0 && remainingSeats > 0){

            if(seatingPlan.equalsIgnoreCase(SeatingPlan.EQUAL.name())){
                // Try to allocate equal number of seats to rows
                seatsInRow = remainingSeats / remainingRows;
            } else {
                // Try to allocate random number of seats to rows
                seatsInRow = random.nextInt(remainingSeats);
            }

            remainingRows--;
            remainingSeats -= seatsInRow;

            // Ensures no seat is left unassigned
            if(remainingRows == 0 && remainingSeats > 0){
                seatsInRow = remainingSeats;
            }

            // create row and add to cache
            Row row = new Row(seatsInRow);
            rowCache.put(row.getId(), row);

            if(logger.isDebugEnabled()){
                logger.debug("Created Row: " + row);
            }

        }

    }

}
