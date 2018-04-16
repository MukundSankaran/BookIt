# BookIt
A simple ticket service that facilitates the discovery, temporary hold, and final reservation of seats within a
high-demand performance venue.


### Supported Functionality:

1. Find the number of available seats in the venue. Available seats are those which are neither held nor reserved.
2. Find and hold the best available seats for a customer, given the number of seats the customer wants and the customer's
unique identifier (E-mail).
3. Reserve and commit a specific group of held seats for a customer.
4. Expire holds after a set period of time.

### Assumptions:

1. Seats closer to the stage are preferred over seats further away.
2. Customers prefer contiguous allocation of requested seats, even if this means sitting further away from the stage.
3. In the event that contiguous allocation of seats within any single row is not possible, customers are assigned seats in
a staggered fashion starting from the row nearest to the stage.
4. All seats within a row are treated the same in terms of customer preference.
5. A given customer cannot have multiple holds or reservations with the same email address for the same event.
6. If an incorrect "seatHoldId" is provided while attempting to reserve a group of held seats, the customer's email can be used to
retrieve the correct group of held seats (assuming the email provided is correct, that is).
7. In all cases where a SeatHold object or Reservation ID cannot be generated, a null is acceptable as the return value.
8. SeatHold expiry times can be configured, and the minimum value is 1 minute and maximum is 30 minutes. A scheduled task runs every
minute to check for and clean up expired holds.
9. Number of rows and seats are integers that need to be greater than or equal to 1
10. The seating arrangement need not be an N x N matrix as provided in the sample arrangement in the problem description.
11. The seating plan can either be 'equal' or 'random'. In the case of 'equal', an attempt is made to allocate equal number of seats
to each row, and in the case of 'random', the number of seats assigned to each row is random.

### Building and Running Tests:

- Requires JDK 8.
- Clone the Git repository.
- Test with
``` shellsession
$> mvn clean test
```
- Run with
``` shellsession
$> mvn spring-boot:run
```
- The external properties file 'application.properties' contains configuration properties for the number of rows, seats,
seating plan, event name and hold expiry time.









