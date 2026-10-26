package integration;

import app.Main;
import config.*;
import dao.*;
import dto.ApiDtos.*;
import helpers.*;
import models.*;
import org.junit.jupiter.api.*;
import server.BookingHttpServer;
import services.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiIntegrationTest {
    private BookingHttpServer server;
    private HttpClient client;
    private AppConfig config;
    private final Map<String,String> codes = new ConcurrentHashMap<>();
    private final Map<Path,byte[]> originalDatabases = new HashMap<>();
    private EmailSender email;
    private AuthService auth;
    private static final String PASSWORD = "SecurePass1!";
    private record AccountFixture(String email,String basic,User user) {}

    @BeforeAll void start() throws Exception {
        for(String name : List.of("data/booking_dev.db","data/booking_test.db")) {
            Path path = Path.of(name);
            if(Files.exists(path)) originalDatabases.put(path,Files.readAllBytes(path));
        }
        config = AppConfig.from(Map.of("APP_ENV","test","PORT","0","DB_URL","jdbc:sqlite:data/booking_dev.db"));
        email = new EmailSender((to,subject,content) -> {
            if(subject.contains("Activation")) {
                var matcher = java.util.regex.Pattern.compile("\\b[0-9]{6}\\b").matcher(content);
                assertTrue(matcher.find()); codes.put(to,matcher.group());
            }
        });
        auth = new AuthService(email);
        server = Main.start(config,email);
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }
    @AfterAll void stop() throws Exception {
        client.close(); server.close();
        for(var entry : originalDatabases.entrySet()) assertArrayEquals(entry.getValue(),Files.readAllBytes(entry.getKey()),"Existing database changed");
        Path db = Path.of(config.dbUrl().substring("jdbc:sqlite:".length()));
        Files.deleteIfExists(db); Files.deleteIfExists(db.getParent());
    }
    private HttpResponse<String> request(String method,String path,Object body,String basic) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getPort() + path)).timeout(Duration.ofSeconds(15));
        if(basic != null) b.header("Authorization",basic);
        if(body != null) b.header("Content-Type","application/json");
        b.method(method,body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body instanceof String s ? s : Json.MAPPER.writeValueAsString(body)));
        return client.send(b.build(),HttpResponse.BodyHandlers.ofString());
    }
    private AccountFixture account(String type) throws Exception {
        String address = UUID.randomUUID() + "@example.com";
        Register input = new Register(type,address,PASSWORD,"Test","Person",User.Gender.NOT_SPECIFIED,"123456",
                "Jamaican",LocalDate.of(1990,1,1),List.of("Quiet","Wi-Fi"));
        var response = request("POST","/api/v1/auth/register",input,null);
        assertEquals(201,response.statusCode(),response.body());
        assertFalse(response.body().contains("password")); assertFalse(response.body().contains("activationCode"));
        assertEquals(200,request("POST","/api/v1/auth/activate",new Activation(address,PASSWORD,codes.get(address)),null).statusCode());
        return new AccountFixture(address,"Basic " + Base64.getEncoder().encodeToString((address + ":" + PASSWORD).getBytes(StandardCharsets.UTF_8)),auth.authenticate(address,PASSWORD));
    }
    private PropertyInput propertyInput(String name) {
        return new PropertyInput(name,"Comfortable accommodation",new AddressData("Main Street","","Kingston","Kingston","","Jamaica"),
                18.0,-76.8,1000.0,2000.0,new BigDecimal("100.00"));
    }
    private String property(AccountFixture owner) throws Exception {
        var response = request("POST","/api/v1/properties",propertyInput("Test house"),owner.basic);
        assertEquals(201,response.statusCode(),response.body());
        assertTrue(response.headers().firstValue("Location").isPresent());
        return Json.MAPPER.readTree(response.body()).get("propertyId").asText();
    }
    private BookingInput booking(String property,String start,String end) {
        return new BookingInput(property,LocalDate.parse(start),LocalDate.parse(end));
    }

    @Test void seededAccountsCanLoginWithoutGainingOtherAccountPrivileges() throws Exception {
        String[][] accounts = {{"odanecollins@live.com","Collins@2026","ADMIN"},
                {"john.smith@example.com","Smith@2026","GUEST"},
                {"phil.collins@example.com","Phil@2026","OWNER"}};
        for(String[] account : accounts) {
            var response = request("POST","/api/v1/auth/login",new Login(account[0],account[1]),null);
            assertEquals(200,response.statusCode(),response.body());
            assertEquals(account[2],Json.MAPPER.readTree(response.body()).get("accountType").asText());
            assertFalse(response.body().contains("passwordHash"));
        }
        String admin = "Basic " + Base64.getEncoder().encodeToString("odanecollins@live.com:Collins@2026".getBytes(StandardCharsets.UTF_8));
        assertEquals(200,request("GET","/api/v1/properties",null,admin).statusCode());
        assertEquals(403,request("GET","/api/v1/bookings",null,admin).statusCode());
        assertEquals(403,request("GET","/api/v1/properties/mine",null,admin).statusCode());
    }

    @Test void healthAndExactMethodRouting() throws Exception {
        assertEquals(200,request("GET","/health",null,null).statusCode());
        assertEquals(200,request("GET","/api/v1/health",null,null).statusCode());
        assertEquals(404,request("GET","/health/extra",null,null).statusCode());
        assertEquals(404,request("GET","/unknown",null,null).statusCode());
        var response = request("POST","/health",null,null);
        assertEquals(405,response.statusCode()); assertEquals("GET",response.headers().firstValue("Allow").orElseThrow());
        assertEquals(405,request("DELETE","/api/v1/properties/mine",null,null).statusCode());
        assertEquals(405,request("PATCH","/api/v1/bookings/test",null,null).statusCode());
        assertEquals(401,request("GET","/api/v1/properties",null,null).statusCode());
    }

    @Test void registrationActivationLoginAndValidation() throws Exception {
        String address = UUID.randomUUID() + "@example.com";
        var input = new Register("GUEST",address,PASSWORD,"Jane","Guest",null,"123","Jamaican",LocalDate.of(1990,1,1),List.of("Quiet"));
        assertEquals(201,request("POST","/api/v1/auth/register",input,null).statusCode());
        assertEquals(403,request("POST","/api/v1/auth/login",new Login(address,PASSWORD),null).statusCode());
        assertEquals(401,request("POST","/api/v1/auth/login",new Login(address,"incorrect"),null).statusCode());
        assertEquals(400,request("POST","/api/v1/auth/activate",new Activation(address,PASSWORD,"wrong"),null).statusCode());
        User pending = UserDAO.getUserByEmail(address);
        pending.setActivationCodeExpiry(LocalDateTime.now().minusMinutes(1)); UserDAO.updateUser(pending);
        assertEquals(400,request("POST","/api/v1/auth/activate",new Activation(address,PASSWORD,codes.get(address)),null).statusCode());
        assertEquals(201,request("POST","/api/v1/auth/register",input,null).statusCode(),"Retry resends activation");
        assertEquals(200,request("POST","/api/v1/auth/activate",new Activation(address,PASSWORD,codes.get(address)),null).statusCode());
        var login = request("POST","/api/v1/auth/login",new Login(address,PASSWORD),null);
        assertEquals(200,login.statusCode()); assertTrue(Json.MAPPER.readTree(login.body()).get("activated").asBoolean());
        assertEquals(409,request("POST","/api/v1/auth/register",input,null).statusCode());
        Guest saved = GuestDAO.getGuestByEmail(address);
        assertEquals(List.of("Quiet"),saved.getPreferences());
        assertEquals(LocalDate.of(1990,1,1),saved.getDob());
        assertNotEquals(PASSWORD,saved.getPasswordHash());
        assertTrue(PasswordSecurity.verifyPassword(PASSWORD,saved.getPasswordHash()));
        assertNull(saved.getActivationCode()); assertNull(saved.getActivationCodeExpiry());
        assertEquals("Guest",UserDAO.getUserByID(saved.getUserID()).getLastName());
        assertEquals(400,request("POST","/api/v1/auth/register",new Register("OWNER","bad","weak","A","B",null,"123",null,null,null),null).statusCode());
    }

    @Test void propertyCrudAndOwnership() throws Exception {
        var owner = account("OWNER"); var other = account("OWNER"); var guest = account("GUEST");
        String id = property(owner);
        assertEquals(200,request("GET","/api/v1/properties/" + id,null,guest.basic).statusCode());
        assertEquals(200,request("GET","/api/v1/properties",null,guest.basic).statusCode());
        var mine = request("GET","/api/v1/properties/mine",null,owner.basic);
        assertEquals(1,Json.MAPPER.readTree(mine.body()).size());
        assertEquals(0,Json.MAPPER.readTree(request("GET","/api/v1/properties/mine",null,other.basic).body()).size());
        assertEquals(403,request("PUT","/api/v1/properties/" + id,propertyInput("Stolen"),other.basic).statusCode());
        assertEquals(403,request("DELETE","/api/v1/properties/" + id,null,other.basic).statusCode());
        assertEquals(403,request("POST","/api/v1/properties",propertyInput("Guest house"),guest.basic).statusCode());
        assertEquals(200,request("PUT","/api/v1/properties/" + id,propertyInput("Updated"),owner.basic).statusCode());
        assertEquals("Updated",PropertyDAO.retrievePropertyByPropertyID(id).getPropertyName());
        assertEquals(204,request("DELETE","/api/v1/properties/" + id,null,owner.basic).statusCode());
        assertEquals(404,request("GET","/api/v1/properties/" + id,null,owner.basic).statusCode());
        assertEquals(404,request("PUT","/api/v1/properties/missing",propertyInput("Missing"),owner.basic).statusCode());
        assertEquals(400,request("POST","/api/v1/properties",new PropertyInput("",null,null,null,null,null,null,null),owner.basic).statusCode());
    }

    @Test void bookingLifecycleConflictsPricesAndOwnership() throws Exception {
        var owner = account("OWNER"); var guest = account("GUEST"); var other = account("GUEST");
        String property = property(owner);
        var response = request("POST","/api/v1/bookings",booking(property,"2030-01-01","2030-01-03"),guest.basic);
        assertEquals(201,response.statusCode(),response.body());
        var json = Json.MAPPER.readTree(response.body());
        String id = json.get("bookingId").asText();
        assertEquals(0,new BigDecimal("238").compareTo(json.get("totalCharge").decimalValue()));
        assertEquals("CONFIRMED",json.get("status").asText()); assertEquals("PENDING",json.get("paymentStatus").asText());
        assertEquals(200,request("GET","/api/v1/bookings/" + id,null,guest.basic).statusCode());
        assertEquals(1,Json.MAPPER.readTree(request("GET","/api/v1/bookings",null,guest.basic).body()).size());
        assertEquals(0,Json.MAPPER.readTree(request("GET","/api/v1/bookings",null,other.basic).body()).size());
        assertEquals(403,request("GET","/api/v1/bookings/" + id,null,other.basic).statusCode());
        assertEquals(403,request("PUT","/api/v1/bookings/" + id,new Dates(LocalDate.of(2030,2,1),LocalDate.of(2030,2,3)),other.basic).statusCode());
        assertEquals(403,request("POST","/api/v1/bookings",booking(property,"2030-02-01","2030-02-03"),owner.basic).statusCode());
        assertEquals(409,request("POST","/api/v1/bookings",booking(property,"2030-01-03","2030-01-05"),other.basic).statusCode());
        assertEquals(409,request("DELETE","/api/v1/properties/" + property,null,owner.basic).statusCode());
        var available = request("GET","/api/v1/properties/available?arrivalDate=2030-01-01&departureDate=2030-01-03",null,guest.basic);
        assertEquals(200,available.statusCode()); assertFalse(available.body().contains(property));
        var change = request("PUT","/api/v1/bookings/" + id,new Dates(LocalDate.of(2030,2,1),LocalDate.of(2030,2,4)),guest.basic);
        assertEquals(200,change.statusCode(),change.body());
        assertEquals(0,new BigDecimal("357").compareTo(Json.MAPPER.readTree(change.body()).get("totalCharge").decimalValue()));
        assertEquals(201,request("POST","/api/v1/bookings",booking(property,"2030-01-01","2030-01-03"),other.basic).statusCode());
        assertEquals(409,request("PUT","/api/v1/bookings/" + id,new Dates(LocalDate.of(2030,1,1),LocalDate.of(2030,1,3)),guest.basic).statusCode());
        assertEquals(LocalDate.of(2030,2,1),BookingDAO.retrieveBookingByBookingID(id).getArrivalDate());
        assertEquals(400,request("POST","/api/v1/bookings",booking(property,"2030-01-01","2030-01-01"),guest.basic).statusCode());
        assertEquals(404,request("POST","/api/v1/bookings",booking("missing","2030-01-01","2030-01-03"),guest.basic).statusCode());
        assertEquals(404,request("GET","/api/v1/bookings/missing",null,guest.basic).statusCode());
    }

    @Test void simultaneousBookingsOnlyOneSucceeds() throws Exception {
        var owner = account("OWNER"); var guest = account("GUEST"); String property = property(owner);
        var body = Json.MAPPER.writeValueAsString(booking(property,"2031-01-01","2031-01-04"));
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getPort() + "/api/v1/bookings"))
                .header("Authorization",guest.basic).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        var first = client.sendAsync(request,HttpResponse.BodyHandlers.ofString());
        var second = client.sendAsync(request,HttpResponse.BodyHandlers.ofString());
        var statuses = new ArrayList<>(List.of(first.get(15,TimeUnit.SECONDS).statusCode(),second.get(15,TimeUnit.SECONDS).statusCode()));
        Collections.sort(statuses); assertEquals(List.of(201,409),statuses);
        assertEquals(1,BookingDAO.retrieveBookingByPropertyID(property).size());
    }

    @Test void malformedAndOversizedRequestsAreControlled() throws Exception {
        assertEquals(400,request("POST","/api/v1/auth/login","{",null).statusCode());
        assertEquals(400,request("POST","/api/v1/auth/login","null",null).statusCode());
        assertEquals(400,request("POST","/api/v1/auth/login","{} {}",null).statusCode());
        assertEquals(400,request("POST","/api/v1/auth/login","{\"unknown\":true}",null).statusCode());
        assertEquals(413,request("POST","/api/v1/auth/login"," ".repeat(65537),null).statusCode());
        assertEquals(415,request("POST","/api/v1/auth/login",null,null).statusCode());
        var badAuth = request("GET","/api/v1/properties",null,"Basic !!!");
        assertEquals(401,badAuth.statusCode()); assertTrue(badAuth.headers().firstValue("WWW-Authenticate").isPresent());
        var guest = account("GUEST");
        assertEquals(400,request("GET","/api/v1/properties/available?arrivalDate=invalid&departureDate=2030-01-01",null,guest.basic).statusCode());
        assertEquals(400,request("GET","/api/v1/properties/available?arrivalDate=2030-01-01",null,guest.basic).statusCode());
        assertEquals(400,request("GET","/api/v1/properties/available?arrivalDate=2030-01-01&arrivalDate=2030-01-02",null,guest.basic).statusCode());
        assertEquals(200,request("GET","/health",null,null).statusCode(),"Server survives invalid requests");
    }

    @Test void foreignKeysAndRegistrationRollback() throws Exception {
        try(Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) {
            try(ResultSet r = s.executeQuery("PRAGMA foreign_keys")) { assertTrue(r.next()); assertEquals(1,r.getInt(1)); }
            assertThrows(SQLException.class,() -> s.executeUpdate("INSERT INTO Properties(propertyID,ownerID) VALUES('orphan','missing')"));
            assertThrows(SQLException.class,() -> s.executeUpdate("INSERT INTO Users(email,passwordHash,isActivated) VALUES('bad-check','hash',2)"));
        }
        var owner = account("OWNER");
        PropertyOwner duplicateId = new PropertyOwner();
        duplicateId.setEmail(UUID.randomUUID() + "@example.com");
        duplicateId.setOwnerID(((PropertyOwner)owner.user).getOwnerID());
        duplicateId.setPasswordHash("hash");
        assertThrows(DataAccessException.class,() -> PropertyOwnerDAO.savePropertyOwner(duplicateId));
        assertNull(UserDAO.getUserByEmail(duplicateId.getEmail()),"Failed subtype insert must roll back base user");
    }

    @Test void serviceRulesAndEmailFailure() throws Exception {
        var owner = account("OWNER"); var guest = account("GUEST"); String id = property(owner);
        var service = new BookingService(new EmailSender((to,subject,body) -> { throw new ServiceException(ServiceException.Reason.UNAVAILABLE,"SMTP failed"); }));
        Booking b = service.create(guest.user,id,LocalDate.of(2033,1,1),LocalDate.of(2033,1,2));
        assertNotNull(BookingDAO.retrieveBookingByBookingID(b.getBookingID()));
        assertThrows(ServiceException.class,() -> service.create(owner.user,id,LocalDate.of(2034,1,1),LocalDate.of(2034,1,2)));
        assertThrows(ServiceException.class,() -> service.create(guest.user,id,LocalDate.of(2034,1,2),LocalDate.of(2034,1,1)));
        assertThrows(ServiceException.class,() -> new PropertyManagementService().list(null));
    }

    @Test void migrationsAreNotReappliedAndTablesExist() throws Exception {
        DBMigration.migrate(config.dbUrl());
        try(Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) {
            try(ResultSet r = s.executeQuery("SELECT count(*) FROM flyway_schema_history WHERE version='1' AND success=1")) {
                assertTrue(r.next()); assertEquals(1,r.getInt(1));
            }
            for(String table : List.of("Users","Guests","PropertyOwners","Properties","Bookings","Employees","Reviews")) {
                try(ResultSet r = s.executeQuery("SELECT count(*) FROM " + table)) { assertTrue(r.next()); }
            }
        }
    }

    @Test void legacyDatesAndHistoricalChargesRemainReadable() throws Exception {
        var owner = account("OWNER"); var guest = account("GUEST"); String property = property(owner);
        Booking b = new BookingService(email).create(guest.user,property,LocalDate.of(2035,1,1),LocalDate.of(2035,1,3));
        try(Connection c = DBUtil.getConnection(); PreparedStatement s = c.prepareStatement(
                "UPDATE Bookings SET arrivalDate=?,departureDate=?,createdAt=?,gct=123,totalCharge=456 WHERE bookingID=?")) {
            s.setString(1,Long.toString(java.sql.Date.valueOf(b.getArrivalDate()).getTime()));
            s.setString(2,Long.toString(java.sql.Date.valueOf(b.getDepartureDate()).getTime()));
            s.setString(3,Long.toString(Timestamp.valueOf(b.getCreatedAt()).getTime()));
            s.setString(4,b.getBookingID()); s.executeUpdate();
        }
        Booking saved = BookingDAO.retrieveBookingByBookingID(b.getBookingID());
        assertEquals(b.getArrivalDate(),saved.getArrivalDate());
        assertEquals(0,new BigDecimal("123").compareTo(saved.getGct()));
        assertEquals(0,new BigDecimal("456").compareTo(saved.getTotalCharge()));
        assertEquals(409,request("POST","/api/v1/bookings",booking(property,"2035-01-02","2035-01-04"),guest.basic).statusCode());
    }

    @Test void internalDatabaseFailureReturnsSafeJsonAndServerRecovers() throws Exception {
        var guest = account("GUEST");
        try(Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) {
            s.execute("ALTER TABLE Properties RENAME TO temporarily_unavailable");
            try {
                var response = request("GET","/api/v1/properties",null,guest.basic);
                assertEquals(500,response.statusCode());
                assertEquals("Internal server error",Json.MAPPER.readTree(response.body()).get("message").asText());
                assertFalse(response.body().contains("SELECT")); assertFalse(response.body().contains("Exception"));
            } finally { s.execute("ALTER TABLE temporarily_unavailable RENAME TO Properties"); }
        }
        assertEquals(200,request("GET","/api/v1/properties",null,guest.basic).statusCode());
    }

    @Test void registrationEmailFailureCanBeRetriedWithoutDuplicateAccounts() throws Exception {
        var failingMail = new EmailSender((to,subject,content) -> {
            throw new ServiceException(ServiceException.Reason.UNAVAILABLE,"Email delivery failed; please try again");
        });
        String address = UUID.randomUUID() + "@example.com";
        Register input = new Register("OWNER",address,PASSWORD,"Jane","Owner",null,"123",null,null,null);
        try(BookingHttpServer failingServer = Main.start(config,failingMail)) {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + failingServer.getPort() + "/api/v1/auth/register"))
                    .header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(Json.MAPPER.writeValueAsString(input))).build();
            var response = client.send(request,HttpResponse.BodyHandlers.ofString());
            assertEquals(503,response.statusCode()); assertFalse(UserDAO.getUserByEmail(address).isActivated());
        }
        assertEquals(201,request("POST","/api/v1/auth/register",input,null).statusCode());
        assertEquals(200,request("POST","/api/v1/auth/activate",new Activation(address,PASSWORD,codes.get(address)),null).statusCode());
        assertInstanceOf(PropertyOwner.class,auth.authenticate(address,PASSWORD));
    }

    @Test void databaseDateConstraintAndBrowserOriginProtection() throws Exception {
        var owner = account("OWNER"); var guest = account("GUEST"); String property = property(owner);
        try(Connection c = DBUtil.getConnection(); PreparedStatement s = c.prepareStatement(
                "INSERT INTO Bookings(bookingID,guestID,propertyID,arrivalDate,departureDate) VALUES(?,?,?,?,?)")) {
            s.setString(1,"invalid-dates"); s.setString(2,((Guest)guest.user).getGuestID()); s.setString(3,property);
            s.setString(4,"2030-01-03"); s.setString(5,"2030-01-01");
            assertThrows(SQLException.class,s::executeUpdate);
        }
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getPort() + "/api/v1/properties/" + property))
                .header("Authorization",owner.basic).header("Origin","https://untrusted.example").DELETE().build();
        assertEquals(403,client.send(request,HttpResponse.BodyHandlers.ofString()).statusCode());
        assertNotNull(PropertyDAO.retrievePropertyByPropertyID(property));
    }
}
