package controller;
import com.sun.net.httpserver.HttpExchange;
import dto.ApiDtos.*;
import http.*;
import services.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

public final class PropertyController {
    private final PropertyManagementService properties;
    private final BookingService bookings;
    private final BasicAuthentication auth;
    public PropertyController(PropertyManagementService properties,BookingService bookings,BasicAuthentication auth) {
        // Main supplies shared services explicitly. Availability belongs to booking logic because
        // it depends on booking dates; property persistence stays in the property service.
        this.properties = properties; this.bookings = bookings; this.auth = auth;
    }
    public void list(HttpExchange x) throws IOException {
        // Authenticate first, then map each model to the public DTO before serializing the collection.
        HttpSupport.send(x,200,properties.list(auth.require(x)).stream().map(PropertyView::from).toList());
    }
    public void mine(HttpExchange x) throws IOException {
        // The service derives the owner ID from the authenticated account, never from a query parameter.
        HttpSupport.send(x,200,properties.mine(auth.require(x)).stream().map(PropertyView::from).toList());
    }
    public void available(HttpExchange x) throws IOException {
        var actor = auth.require(x);
        // Allow only these two query keys; the helper also rejects duplicates to avoid ambiguous dates.
        var query = HttpSupport.query(x,Set.of("arrivalDate","departureDate"));
        if(query.size() != 2) throw new HttpException(400,"arrivalDate and departureDate are required");
        // Parse ISO dates here; the service validates their order and applies the overlap rule.
        // A malformed date propagates to AppRouter and becomes a controlled 400 response.
        HttpSupport.send(x,200,bookings.available(actor,LocalDate.parse(query.get("arrivalDate")),
                LocalDate.parse(query.get("departureDate"))).stream().map(PropertyView::from).toList());
    }
    public void get(HttpExchange x,String id) throws IOException {
        // AppRouter extracted id from the path. The service handles a missing property.
        HttpSupport.send(x,200,PropertyView.from(properties.find(auth.require(x),id)));
    }
    public void create(HttpExchange x) throws IOException {
        var actor = auth.require(x);
        // Convert editable fields to a model. The service assigns its ID and authenticated owner.
        var property = properties.create(actor,HttpSupport.body(x,PropertyInput.class).model());
        // Set headers before sending the body. Location tells the client where to retrieve the new resource.
        x.getResponseHeaders().set("Location","/api/v1/properties/" + property.getPropertyID());
        HttpSupport.send(x,201,PropertyView.from(property));
    }
    public void update(HttpExchange x,String id) throws IOException {
        var actor = auth.require(x);
        // PUT replaces supported details. The service verifies ownership and retains the stored ID/owner.
        HttpSupport.send(x,200,PropertyView.from(properties.update(actor,id,HttpSupport.body(x,PropertyInput.class).model())));
    }
    public void delete(HttpExchange x,String id) throws IOException {
        // A referenced property may fail deletion with a conflict. Send 204 (no body) only on success.
        properties.delete(auth.require(x),id); HttpSupport.send(x,204,null);
    }
}
