package helpers;
import models.Booking;
import models.Property;
import services.EmailService;
import services.MailSender;

/** Templates stay reusable; injecting the transport lets tests capture mail without contacting SMTP. */
public class EmailSender {
    private final MailSender mail;
    public EmailSender() { this(new EmailService()); }
    public EmailSender(MailSender mail) { this.mail = mail; }
    public void sendActivationEmail(String to, String code) {
        mail.sendEmail(to,"Osyntra - Account Activation","Your activation code is " + code + ". It expires in 10 minutes.");
    }
    public void sendBookingConfirmationEmail(String to, Booking booking, Property property) {
        mail.sendEmail(to,"Osyntra - Booking Confirmation " + booking.getBookingID(),
                "Booking " + booking.getBookingID() + " at " + property.getPropertyName() + "\n"
                + booking.getArrivalDate() + " to " + booking.getDepartureDate() + "\nTotal: " + booking.getTotalCharge());
    }
}
