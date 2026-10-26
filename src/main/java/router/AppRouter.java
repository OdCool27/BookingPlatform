package router;

import com.sun.net.httpserver.*;
import controller.*;
import dao.DataAccessException;
import dto.ApiDtos.Message;
import http.*;
import server.BookingHttpServer;
import services.ServiceException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.logging.*;

/** HttpServer contexts match prefixes only. One root context delegates to this exact path/method router. */
public final class AppRouter implements HttpHandler {
    // Each endpoint receives the exchange plus an optional ID captured from the resource path.
    // This lets literal routes and parameterized routes share the same dispatch mechanism.
    @FunctionalInterface private interface Endpoint { void handle(HttpExchange exchange,String id) throws IOException; }
    private record Route(String method,Pattern path,Endpoint endpoint) {}
    private final List<Route> routes = new ArrayList<>();
    private final Set<String> trustedOrigins;
    private static final Logger LOG = Logger.getLogger(AppRouter.class.getName());
    public AppRouter(AuthController auth,PropertyController properties,BookingController bookings) {
        this(auth,properties,bookings,System.getenv().getOrDefault("TRUSTED_FRONTEND_ORIGINS",""));
    }
    public AppRouter(AuthController auth,PropertyController properties,BookingController bookings,String origins) {
        // Exact origins only; an empty configuration fails closed for browser requests.
        trustedOrigins = new HashSet<>();
        for(String origin : origins.split(",")) {
            if(origin.isBlank()) continue;
            String value = origin.trim();
            java.net.URI uri = java.net.URI.create(value);
            if(!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) || uri.getHost() == null ||
                    uri.getRawUserInfo() != null || !uri.getRawPath().isEmpty() ||
                    uri.getRawQuery() != null || uri.getRawFragment() != null)
                throw new IllegalArgumentException("Trusted frontend origins must be exact HTTP(S) origins");
            trustedOrigins.add(value);
        }
        // Store controller callbacks during startup; they are invoked later for matching requests.
        // Authentication is performed by protected controllers, leaving health/auth routes accessible.
        route("GET","/health",(x,id) -> HttpSupport.send(x,200,new Message("Server is healthy")));
        route("GET","/api/v1/health",(x,id) -> HttpSupport.send(x,200,new Message("Server is healthy")));
        route("POST","/api/v1/auth/register",(x,id) -> auth.register(x));
        route("POST","/api/v1/auth/activate",(x,id) -> auth.activate(x));
        route("POST","/api/v1/auth/login",(x,id) -> auth.login(x));
        route("GET","/api/v1/properties",(x,id) -> properties.list(x));
        route("POST","/api/v1/properties",(x,id) -> properties.create(x));
        route("GET","/api/v1/properties/mine",(x,id) -> properties.mine(x));
        route("GET","/api/v1/properties/available",(x,id) -> properties.available(x));
        route("GET","/api/v1/properties/{id}",properties::get);
        route("PUT","/api/v1/properties/{id}",properties::update);
        route("DELETE","/api/v1/properties/{id}",properties::delete);
        route("GET","/api/v1/bookings",(x,id) -> bookings.list(x));
        route("POST","/api/v1/bookings",(x,id) -> bookings.create(x));
        route("GET","/api/v1/bookings/{id}",bookings::get);
        route("PUT","/api/v1/bookings/{id}",bookings::update);
    }
    private void route(String method,String path,Endpoint endpoint) {
        // ^ and $ require a full path match. The parentheses capture {id} for the controller;
        // the restricted character set accepts the application's IDs without accepting extra slashes.
        routes.add(new Route(method,Pattern.compile("^" + path.replace("{id}","([A-Za-z0-9-]+)") + "$"),endpoint));
    }
    public void registerRoutes(BookingHttpServer server) {
        // HttpServer routes every path under / here; this class then selects the exact REST endpoint.
        server.registerRoute("/",this);
    }
    @Override public void handle(HttpExchange x) throws IOException {
        try {
            // Use the raw path so encoded separators cannot turn into a different resource path.
            String path = x.getRequestURI().getRawPath();
            if(path.length() > 2048) throw new HttpException(414,"Request path is too long");
            // The same-origin frontend proxy preserves Origin. Never infer trust from Host,
            // strip Origin, or use suffix matching. Non-browser clients remain supported.
            var origins = x.getRequestHeaders().get("Origin");
            if(origins != null && (origins.size() != 1 || !trustedOrigins.contains(origins.getFirst())))
                throw new HttpException(403,"Frontend origin is not trusted");
            // Match paths before methods: no path match means 404, while a method mismatch means 405.
            List<Route> matches = routes.stream().filter(r -> r.path.matcher(path).matches()).toList();
            // Literal paths (mine/available) take precedence over the {id} route for every method.
            // A literal regex has zero capture groups. Otherwise DELETE /properties/mine could
            // accidentally be treated as deletion of a property whose ID is "mine".
            if(matches.stream().anyMatch(r -> r.path.matcher(path).groupCount() == 0))
                matches = matches.stream().filter(r -> r.path.matcher(path).groupCount() == 0).toList();
            if(matches.isEmpty()) throw new HttpException(404,"Route not found");
            for(Route r : matches) {
                if(r.method.equals(x.getRequestMethod())) {
                    // matches() positions the matcher so group(1) can retrieve the captured ID.
                    // Literal routes receive null because their controllers do not need an ID.
                    var matcher = r.path.matcher(path); matcher.matches();
                    r.endpoint.handle(x,matcher.groupCount() == 0 ? null : matcher.group(1));
                    return;
                }
            }
            // The resource path exists, but this verb is unsupported. Tell clients which verbs it accepts.
            x.getResponseHeaders().set("Allow",String.join(", ",matches.stream().map(Route::method).distinct().toList()));
            throw new HttpException(405,"Method not allowed");
        } catch(HttpException e) {
            HttpSupport.error(x,e.status,"HTTP_" + e.status,e.getMessage());
        } catch(ServiceException e) {
            // Services describe business failures without importing HTTP classes. Translate those
            // reasons here so all controllers use the same status and error-body conventions.
            int status = switch(e.reason()) {
                case INVALID -> 400; case UNAUTHENTICATED -> 401; case FORBIDDEN -> 403;
                case NOT_FOUND -> 404; case CONFLICT -> 409; case UNAVAILABLE -> 503;
            };
            HttpSupport.error(x,status,e.reason().name(),e.getMessage());
        } catch(java.time.DateTimeException | IllegalArgumentException e) {
            HttpSupport.error(x,400,"INVALID","Invalid request parameter");
        } catch(DataAccessException e) {
            // Constraint failures represent data conflicts. Other database failures are logged
            // internally; the response must not reveal SQL, paths or driver exception messages.
            if(e.isConstraintViolation()) HttpSupport.error(x,409,"CONFLICT","Operation conflicts with existing data");
            else { LOG.log(Level.SEVERE,"Database request failed",e); HttpSupport.error(x,500,"INTERNAL","Internal server error"); }
        } catch(Exception e) {
            LOG.log(Level.SEVERE,"Request failed",e);
            HttpSupport.error(x,500,"INTERNAL","Internal server error");
        } finally {
            // Release request/response streams on both successful and failed requests.
            x.close();
        }
    }
}
