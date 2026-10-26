package integration;

import config.DBMigration;
import dto.ApiDtos.BookingView;
import helpers.EmailSender;
import helpers.Json;
import models.Booking;
import models.Property;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class BookingReferenceTest {
    @TempDir Path directory;

    private Connection connect(String url) throws SQLException {
        Connection c = DriverManager.getConnection(url);
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys=ON");
            s.execute("PRAGMA busy_timeout=10000");
        }
        return c;
    }
    private void insert(Connection c,String id) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("INSERT INTO Bookings(bookingID,guestID,propertyID,arrivalDate,departureDate) VALUES(?,'guest','property','2030-01-01','2030-01-02')")) {
            s.setString(1,id); s.executeUpdate();
        }
    }
    private String reference(Connection c,String id) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT bookingReference FROM Bookings WHERE bookingID=?")) {
            s.setString(1,id);
            try (ResultSet r = s.executeQuery()) { assertTrue(r.next()); return r.getString(1); }
        }
    }
    @Test void backfillSequenceConcurrencyImmutabilityAndUuidRelationships() throws Exception {
        String url = "jdbc:sqlite:" + directory.resolve("references.db");
        Flyway.configure().dataSource(url,null,null).target("1").load().migrate();
        String original = UUID.randomUUID().toString();
        try (Connection c = connect(url); Statement s = c.createStatement()) {
            s.execute("INSERT INTO Users(email,passwordHash) VALUES('guest@example.com','hash'),('owner@example.com','hash')");
            s.execute("INSERT INTO Guests(guestID,email) VALUES('guest','guest@example.com')");
            s.execute("INSERT INTO PropertyOwners(ownerID,email) VALUES('owner','owner@example.com')");
            s.execute("INSERT INTO Properties(propertyID,ownerID) VALUES('property','owner')");
            insert(c,original);
            try (PreparedStatement review = c.prepareStatement("INSERT INTO Reviews(guestID,propertyID,bookingID,rating) VALUES('guest','property',?,5)")) {
                review.setString(1,original); review.executeUpdate();
            }
        }
        DBMigration.migrate(url);
        try (Connection c = connect(url); Statement s = c.createStatement()) {
            assertEquals("100001",reference(c,original));
            try (ResultSet r = s.executeQuery("SELECT b.bookingReference FROM Reviews r JOIN Bookings b ON r.bookingID=b.bookingID")) {
                assertTrue(r.next()); assertEquals("100001",r.getString(1));
            }
            insert(c,"second"); assertEquals("100002",reference(c,"second"));
            assertThrows(SQLException.class,() -> s.executeUpdate("UPDATE Bookings SET bookingReference='100003' WHERE bookingID='second'"));
            assertThrows(SQLException.class,() -> s.executeUpdate("UPDATE Bookings SET bookingReference=NULL WHERE bookingID='second'"));
            assertThrows(SQLException.class,() -> s.executeUpdate("INSERT INTO Bookings(bookingID,guestID,propertyID,arrivalDate,departureDate,bookingReference) VALUES('spoof','guest','property','2030-01-01','2030-01-02','100001')"));
            s.executeUpdate("UPDATE Bookings SET status='CANCELLED' WHERE bookingID='second'");
            assertEquals("100002",reference(c,"second"));
            s.executeUpdate("DELETE FROM Bookings WHERE bookingID='second'");
        }
        // Reopen connections and rerun startup migrations: the high-water mark must survive both.
        DBMigration.migrate(url);
        try (Connection c = connect(url)) {
            insert(c,"third"); assertEquals("100003",reference(c,"third"));
        }
        try (var pool = Executors.newFixedThreadPool(4)) {
            List<Callable<String>> inserts = new ArrayList<>();
            for(int i=0;i<12;i++) inserts.add(() -> {
                try (Connection c = connect(url)) {
                    String id = UUID.randomUUID().toString(); insert(c,id); return reference(c,id);
                }
            });
            Set<String> allocated = new TreeSet<>();
            for(Future<String> f : pool.invokeAll(inserts)) assertTrue(allocated.add(f.get()));
            assertEquals("100004",allocated.iterator().next());
            assertTrue(allocated.contains("100015"));
        }
        try (Connection c = connect(url); Statement s = c.createStatement()) {
            // Exhaustion fails rather than issuing seven digits or recycling a reference.
            s.executeUpdate("UPDATE BookingReferenceSequence SET lastReference=999999");
            assertThrows(SQLException.class,() -> insert(c,"exhausted"));
            try (ResultSet r = s.executeQuery("PRAGMA foreign_key_check")) { assertFalse(r.next()); }
        }
    }

    @Test void customerDtoAndEmailContainReferenceNotUuid() throws Exception {
        Booking b = new Booking(); b.setBookingID(UUID.randomUUID().toString()); b.setBookingReference("100001");
        assertEquals("100001",new Booking(b).getBookingReference());
        assertThrows(IllegalStateException.class,() -> b.setBookingReference("100002"));
        String json = Json.MAPPER.writeValueAsString(BookingView.from(b));
        assertTrue(json.contains("\"bookingReference\":\"100001\""));
        assertFalse(json.contains("bookingId")); assertFalse(json.contains(b.getBookingID()));
        List<String> messages = new ArrayList<>();
        new EmailSender((to,subject,body) -> { messages.add(subject); messages.add(body); })
                .sendBookingConfirmationEmail("guest@example.com",b,new Property());
        for(String text : messages) { assertTrue(text.contains("100001")); assertFalse(text.contains(b.getBookingID())); }
    }
}
