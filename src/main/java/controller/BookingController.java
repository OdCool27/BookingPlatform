package controller;
import com.sun.net.httpserver.HttpExchange;
import dto.ApiDtos.*;
import http.*;
import services.BookingService;
import java.io.IOException;

public final class BookingController {
    private final BookingService service;
    private final BasicAuthentication auth;
    public BookingController(BookingService service,BasicAuthentication auth) { this.service = service; this.auth = auth; }
    public void list(HttpExchange x) throws IOException {
        // History is scoped by the authenticated guest; clients cannot select another guest's ID.
        // Map each result to the response DTO so internal model fields stay out of JSON.
        HttpSupport.send(x,200,service.history(auth.require(x)).stream().map(BookingView::from).toList());
    }
    public void get(HttpExchange x,String id) throws IOException {
        // The service checks that this path ID belongs to the authenticated guest before returning it.
        HttpSupport.send(x,200,BookingView.from(service.find(auth.require(x),id)));
    }
    public void create(HttpExchange x) throws IOException {
        // Authenticate before reading business input. The DTO accepts property/dates only;
        // the service derives guest identity, charges, status and the new booking ID.
        var actor = auth.require(x); var input = HttpSupport.body(x,BookingInput.class);
        var booking = service.create(actor,input.propertyId(),input.arrivalDate(),input.departureDate());
        // Creation has completed at this point; expose the new resource URL and safe response fields.
        x.getResponseHeaders().set("Location","/api/v1/bookings/" + booking.getBookingID());
        HttpSupport.send(x,201,BookingView.from(booking));
    }
    public void update(HttpExchange x,String id) throws IOException {
        // Only dates are editable in V1. Ownership, repricing and atomic conflict checking remain
        // in the service/DAO; exceptions propagate to AppRouter for a consistent error response.
        var actor = auth.require(x); var input = HttpSupport.body(x,Dates.class);
        HttpSupport.send(x,200,BookingView.from(service.changeDates(actor,id,input.arrivalDate(),input.departureDate())));
    }
}
