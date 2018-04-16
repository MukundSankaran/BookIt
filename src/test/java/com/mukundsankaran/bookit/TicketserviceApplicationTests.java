package com.mukundsankaran.bookit;

import com.mukundsankaran.bookit.config.BookItProperties;
import com.mukundsankaran.bookit.model.SeatHold;
import com.mukundsankaran.bookit.service.TicketService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test.properties")
public class TicketserviceApplicationTests {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private BookItProperties bookItProperties;

	@Test
	public void contextLoads() {

	}

    @Test
    public void testNumSeatsAvailable(){
        final int numSeats = bookItProperties.getVenue().getCapacity();
        int numSeatsAvailable = ticketService.numSeatsAvailable();

        // Check if the number of seats set by config is the same as the number of seats available
        Assert.assertEquals(numSeats, numSeatsAvailable);
    }

    @Test
    public void testFindAndHoldSeats(){

        int requestedSeats = 10;
        int numSeatsBeforeHold = ticketService.numSeatsAvailable();
        String customerEmail = "abc@gmail.com";

        // Check if number of seats before hold is more than the requested number
        Assert.assertTrue(numSeatsBeforeHold >= requestedSeats);

        SeatHold seatHold = ticketService.findAndHoldSeats(requestedSeats, customerEmail);

        // Check if the returned SeatHold is not null
        Assert.assertNotNull(seatHold);

        int numSeatsAvailable = ticketService.numSeatsAvailable();

        // Check if the number of seats available has been updated to reflect the SeatHold
        Assert.assertEquals(numSeatsBeforeHold - requestedSeats, numSeatsAvailable);

        SeatHold seatHoldDuplicate = ticketService.findAndHoldSeats(requestedSeats, customerEmail);

        // Check if duplicate holds for the same user and for the same event are disallowed
        Assert.assertNull(seatHoldDuplicate);
    }

    @Test
    public void testReserveSeats(){

        int requestedSeats = 10;
        int numSeatsBeforeHold = ticketService.numSeatsAvailable();
        String customerEmail = "abc@gmail.com";

        // Check if number of seats before hold is more than the requested number
        Assert.assertTrue(numSeatsBeforeHold >= requestedSeats);

        SeatHold seatHold = ticketService.findAndHoldSeats(requestedSeats, customerEmail);

        // Check if the returned SeatHold is not null
        Assert.assertNotNull(seatHold);

        int numSeatsAvailable = ticketService.numSeatsAvailable();

        // Check if the number of seats available has been updated to reflect the SeatHold
        Assert.assertEquals(numSeatsBeforeHold - requestedSeats, numSeatsAvailable);

        String reservationId = ticketService.reserveSeats(seatHold.getId(), customerEmail);

        // Check if the SeatHold has been reserved successfully
        Assert.assertNotNull(reservationId);

        String reservationIdDuplicate = ticketService.reserveSeats(seatHold.getId(), customerEmail);

        // Check if duplicate reservation requests for the same user and for the same event are disallowed
        Assert.assertNull(reservationIdDuplicate);
    }

}
