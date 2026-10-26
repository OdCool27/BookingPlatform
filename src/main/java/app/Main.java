package app;

import config.*;
import controller.*;
import helpers.EmailSender;
import http.BasicAuthentication;
import router.AppRouter;
import server.BookingHttpServer;
import services.*;
import java.io.IOException;
import java.util.logging.Logger;

public final class Main {
    public static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) throws IOException {
        AppConfig config = AppConfig.load();
        EmailService mail = new EmailService();
        if(config.environment().equals("prod") || mail.hasConfiguration()) mail.validateConfiguration();
        BookingHttpServer server = start(config,new EmailSender(mail));
        Runtime.getRuntime().addShutdownHook(new Thread(server::close,"osyntra-shutdown"));
        LOGGER.info("Osyntra " + config.environment() + " listening on http://127.0.0.1:" + server.getPort());
    }

    /** This is the composition root: dependencies are constructed once and passed explicitly.
     * Configure JDBC and migrate before allocating the listening socket. Any failure propagates
     * to main, preventing a partially initialized application from accepting requests.
     */
    public static BookingHttpServer start(AppConfig config,EmailSender email) throws IOException {
        DBUtil.configure(config);
        DBMigration.migrate(config.dbUrl());
        AuthService auth = new AuthService(email);
        PropertyManagementService properties = new PropertyManagementService();
        BookingService bookings = new BookingService(email);
        BasicAuthentication security = new BasicAuthentication(auth);
        AppRouter router = new AppRouter(new AuthController(auth),
                new PropertyController(properties,bookings,security),new BookingController(bookings,security));
        BookingHttpServer server = new BookingHttpServer(config.serverPort());
        try { router.registerRoutes(server); server.start(); return server; }
        catch(RuntimeException e) { server.close(); throw e; }
    }
}
