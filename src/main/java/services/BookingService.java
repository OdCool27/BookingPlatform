package services;

import dao.*;
import helpers.EmailSender;
import models.*;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;
import static services.ServiceException.Reason.*;

public class BookingService {
    private final EmailSender email;
    public BookingService() { this(new EmailSender()); }
    public BookingService(EmailSender email) { this.email = email; }

    public List<Property> available(User actor,LocalDate start,LocalDate end) {
        AuthService.requireActive(actor); return listAvailableProperties(start,end);
    }
    public List<Property> listAvailableProperties(LocalDate start,LocalDate end) {
        Validation.dates(start,end);
        List<Booking> bookings = BookingDAO.retrieveAllBookings();
        return PropertyDAO.retrieveAllProperties().stream().filter(p -> bookings.stream().noneMatch(b ->
                b.getPropertyID().equals(p.getPropertyID()) && b.getStatus() == Booking.Status.CONFIRMED &&
                !(b.getArrivalDate().isAfter(end) || b.getDepartureDate().isBefore(start)))).toList();
    }
    public List<Booking> history(User actor) {
        return BookingDAO.retrieveBookingByGuestID(AuthService.guest(actor).getGuestID());
    }
    public Booking find(User actor,String id) {
        Guest g = AuthService.guest(actor);
        Booking b = BookingDAO.retrieveBookingByBookingID(id);
        if(b == null) throw new ServiceException(NOT_FOUND,"Booking not found");
        if(!b.getGuestID().equals(g.getGuestID())) throw new ServiceException(FORBIDDEN,"Booking belongs to another guest");
        return b;
    }
    public Booking create(User actor,String propertyId,LocalDate arrival,LocalDate departure) {
        Guest guest = AuthService.guest(actor); Validation.dates(arrival,departure);
        Validation.text(propertyId,"propertyId",100);
        Property p = PropertyDAO.retrievePropertyByPropertyID(propertyId);
        if(p == null) throw new ServiceException(NOT_FOUND,"Property not found");
        Booking b = new Booking();
        b.setBookingID("B-" + UUID.randomUUID());
        b.setGuestID(guest.getGuestID()); b.setPropertyID(propertyId);
        price(b,p,arrival,departure);
        if(!confirmBooking(b,guest,p)) throw new ServiceException(CONFLICT,"Property is not available for these dates");
        return b;
    }
    public Booking changeDates(User actor,String id,LocalDate arrival,LocalDate departure) {
        Booking b = find(actor,id); Validation.dates(arrival,departure);
        Property p = PropertyDAO.retrievePropertyByPropertyID(b.getPropertyID());
        if(p == null) throw new ServiceException(NOT_FOUND,"Property not found");
        price(b,p,arrival,departure); b.setUpdatedAt(LocalDateTime.now());
        if(!BookingDAO.updateBooking(b)) throw new ServiceException(CONFLICT,"Property is not available for these dates");
        return b;
    }
    private static void price(Booking b,Property p,LocalDate arrival,LocalDate departure) {
        b.setArrivalDate(arrival); b.setDepartureDate(departure);
        b.setBaseCharge(p.getBaseRate().multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(arrival,departure))));
    }
    private boolean confirmBooking(Booking b,Guest guest,Property p) {
        Validation.dates(b.getArrivalDate(),b.getDepartureDate());
        if(!BookingDAO.saveBooking(b)) return false;
        // Email is an existing side effect. A transport outage must not turn a committed booking
        // into an apparent failure and encourage clients to submit duplicate bookings.
        try { email.sendBookingConfirmationEmail(guest.getEmail(),b,p); }
        catch(ServiceException e) { Logger.getLogger(BookingService.class.getName()).warning("Booking saved; confirmation email delivery failed"); }
        return true;
    }
}
