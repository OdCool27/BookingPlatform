package controller;
import com.sun.net.httpserver.HttpExchange;
import dto.ApiDtos.*;
import http.HttpSupport;
import models.*;
import services.*;
import java.io.IOException;
import java.util.ArrayList;

public final class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }
    public void register(HttpExchange exchange) throws IOException {
        // HttpExchange represents one request/response pair. Decode its JSON into the allowed
        // request fields, rather than letting the client populate a User model directly.
        Register input = HttpSupport.body(exchange,Register.class);
        // Map the existing account type to its model and collect subtype-specific profile fields.
        // IDs, password hashes and activation state are assigned by the service, not this request.
        User user;
        if("GUEST".equals(input.accountType())) {
            Guest g = new Guest(); g.setNationality(input.nationality()); g.setDob(input.dob());
            g.setPreferences(input.preferences() == null ? new ArrayList<>() : input.preferences()); user = g;
        } else if("OWNER".equals(input.accountType())) user = new PropertyOwner();
        else throw new ServiceException(ServiceException.Reason.INVALID,"accountType must be GUEST or OWNER");
        // Both account types share these profile fields; omitted gender uses the existing default.
        user.setEmail(input.email()); user.setFirstName(input.firstName()); user.setLastName(input.lastName());
        user.setGender(input.gender() == null ? User.Gender.NOT_SPECIFIED : input.gender());
        user.setContactNum(input.contactNum());
        // The service validates, hashes, persists and sends activation mail. Only its successful
        // result becomes a 201 response; Account.from omits password hashes and activation codes.
        HttpSupport.send(exchange,201,Account.from(service.register(user,input.password())));
    }
    public void login(HttpExchange exchange) throws IOException {
        Login input = HttpSupport.body(exchange,Login.class);
        // Login checks credentials and activation; it returns safe account details, not a session token.
        HttpSupport.send(exchange,200,Account.from(service.authenticate(input.email(),input.password())));
    }
    public void activate(HttpExchange exchange) throws IOException {
        Activation input = HttpSupport.body(exchange,Activation.class);
        // Keep code expiry/password checks in the service. Failures reach the router's error mapping,
        // so this success message is sent only after activation has been persisted.
        service.activate(input.email(),input.password(),input.code());
        HttpSupport.send(exchange,200,new Message("Account activated"));
    }
}
