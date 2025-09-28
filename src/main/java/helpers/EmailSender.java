package helpers;


import dao.PropertyDAO;
import models.Booking;
import models.Property;
import services.EmailService;

public class EmailSender {
    public void sendTestEmail(String email){
        EmailService e = new EmailService();
        String content = "TEST!";
        String subject = "VC TEST EMAIL";
        e.sendEmail(email, subject, content);
    }

    public void sendActivationEmail(String toEmail, String activationCode) {
        EmailService e = new EmailService();
        String content = "Greetings,\n"
                + "Below contains the activation code to active your Vapfumi Connect Guest Account.\n"
                + "If you did not sign up on Vapfumi Connect, kindly disregard this email.\n"
                + "Activation Code: " + activationCode
                + "\n\nRegards,\n"
                +"Vapfumi Connect Support.";
        e.sendEmail(toEmail, "Vapfumi Connect - Account Activation", content);
    }

    public void sendBookingConfirmationEmail(String toEmail, Booking booking, Property p){
        EmailService e =  new EmailService();
        String content = booking.toString();

        e.sendEmail(toEmail, "Vapfumi Connect - Booking Confirmation# " + booking.getBookingID(), content);
    }



    //TEST
    public static void main(String[] args) {
        EmailSender e = new EmailSender();
        e.sendTestEmail("odanecollins@gmail.com");
    }

}

