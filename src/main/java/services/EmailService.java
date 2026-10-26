package services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Map;
import java.util.Properties;
import static services.ServiceException.Reason.UNAVAILABLE;

/** Credentials come from the environment, never a source-tree properties file. */
public class EmailService implements MailSender {
    private final Map<String,String> config;
    public EmailService() { this(System.getenv()); }
    public EmailService(Map<String,String> config) { this.config = Map.copyOf(config); }
    public boolean hasConfiguration() {
        return java.util.List.of("SMTP_HOST","SMTP_USERNAME","SMTP_PASSWORD","SMTP_PORT").stream().anyMatch(config::containsKey);
    }
    public boolean configured() {
        return java.util.List.of("SMTP_HOST","SMTP_USERNAME","SMTP_PASSWORD").stream()
                .allMatch(k -> config.containsKey(k) && !config.get(k).isBlank());
    }
    public void validateConfiguration() {
        if (!configured()) throw new IllegalArgumentException("SMTP_HOST, SMTP_USERNAME and SMTP_PASSWORD are required");
        int port;
        try { port = Integer.parseInt(config.getOrDefault("SMTP_PORT","587")); }
        catch(NumberFormatException e) { throw new IllegalArgumentException("SMTP_PORT must be an integer"); }
        if(port < 1 || port > 65535) throw new IllegalArgumentException("SMTP_PORT is out of range");
    }
    @Override public void sendEmail(String to, String subject, String content) {
        if(!configured()) throw new ServiceException(UNAVAILABLE,"Email delivery is not configured");
        validateConfiguration();
        Properties props = new Properties();
        props.setProperty("mail.smtp.host",config.get("SMTP_HOST"));
        props.setProperty("mail.smtp.port",config.getOrDefault("SMTP_PORT","587"));
        props.setProperty("mail.smtp.auth","true");
        props.setProperty("mail.smtp.starttls.enable","true");
        props.setProperty("mail.smtp.starttls.required","true");
        props.setProperty("mail.smtp.ssl.checkserveridentity","true");
        props.setProperty("mail.smtp.connectiontimeout","5000");
        props.setProperty("mail.smtp.timeout","5000");
        props.setProperty("mail.smtp.writetimeout","5000");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.get("SMTP_USERNAME"),config.get("SMTP_PASSWORD"));
            }
        });
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.get("SMTP_USERNAME")));
            message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));
            message.setSubject(subject,"UTF-8"); message.setText(content,"UTF-8");
            Transport.send(message);
        } catch(MessagingException e) {
            throw new ServiceException(UNAVAILABLE,"Email delivery failed; please try again");
        }
    }
}
