package http;
import com.sun.net.httpserver.HttpExchange;
import models.User;
import services.AuthService;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** HTTP Basic is only a transport for the existing email/password check.
 * No session, token store or new roles are introduced. Use TLS outside loopback.
 */
public final class BasicAuthentication {
    private final AuthService service;
    public BasicAuthentication(AuthService service) { this.service = service; }
    public User require(HttpExchange exchange) {
        // Expect "Authorization: Basic <base64>" and bound its size before decoding credentials.
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if(header == null || header.length() > 1024 || !header.regionMatches(true,0,"Basic ",0,6))
            throw new HttpException(401,"Basic authentication required");
        try {
            // Base64 is encoding, not encryption; transport security must come from HTTPS.
            // Split on the first colon because the password itself may contain a colon.
            String credentials = new String(Base64.getDecoder().decode(header.substring(6)),StandardCharsets.UTF_8);
            int separator = credentials.indexOf(':');
            if(separator < 1) throw new HttpException(401,"Invalid credentials");
            // The service verifies BCrypt, activation and account type on every protected request.
            return service.authenticate(credentials.substring(0,separator),credentials.substring(separator+1));
        } catch(IllegalArgumentException e) { throw new HttpException(401,"Invalid credentials"); }
    }
}
