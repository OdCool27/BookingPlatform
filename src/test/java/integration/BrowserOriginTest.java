package integration;

import controller.*;
import dto.ApiDtos.*;
import helpers.Json;
import models.PropertyOwner;
import org.junit.jupiter.api.Test;
import router.AppRouter;
import server.BookingHttpServer;
import services.AuthService;
import java.net.URI;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;

class BrowserOriginTest {
    @Test void exactTrustedOriginCanWriteAndUntrustedOriginsAreRejected() throws Exception {
        // Exercise the real HTTP router; this narrow fixture isolates origin handling from SMTP/DB.
        AuthService auth = new AuthService(null) {
            @Override public models.User authenticate(String email, String password) {
                PropertyOwner owner = new PropertyOwner(); owner.setEmail(email); owner.setActivated(true); return owner;
            }
        };
        try (BookingHttpServer server = new BookingHttpServer(0); HttpClient client = HttpClient.newHttpClient()) {
            new AppRouter(new AuthController(auth), new PropertyController(null,null,null), new BookingController(null,null), "http://127.0.0.1:5173,https://booking.example").registerRoutes(server);
            server.start();
            for (String origin : new String[]{"http://127.0.0.1:5173", "https://booking.example", "https://untrusted.example", "https://booking.example.evil", "http://127.0.0.1:5174", "null"}) {
                var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getPort() + "/api/v1/auth/login"))
                    .header("Origin", origin).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(Json.MAPPER.writeValueAsString(new Login("test@example.com", "unused")))).build();
                int expected = origin.equals("http://127.0.0.1:5173") || origin.equals("https://booking.example") ? 200 : 403;
                assertEquals(expected, client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode(), origin);
            }
        }
    }
    @Test void emptyAllowlistFailsClosedAndWildcardConfigurationFails() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> new AppRouter(null,null,null,"*"));
        assertThrows(IllegalArgumentException.class, () -> new AppRouter(null,null,null,"https://example.com/path"));
        try (BookingHttpServer server = new BookingHttpServer(0); HttpClient client = HttpClient.newHttpClient()) {
            new AppRouter(null,new PropertyController(null,null,null),new BookingController(null,null),"").registerRoutes(server); server.start();
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getPort() + "/health"));
            assertEquals(200,client.send(request.GET().build(),HttpResponse.BodyHandlers.ofString()).statusCode());
            assertEquals(403,client.send(request.header("Origin","http://127.0.0.1:5173").build(),HttpResponse.BodyHandlers.ofString()).statusCode());
        }
    }
}
