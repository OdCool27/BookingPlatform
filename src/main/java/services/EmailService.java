package services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EmailService {

    private final Properties smtpProps = new Properties();
    String propertiesFilePath = "src/smtp.properties";


    public EmailService(){
        try(FileInputStream input = new FileInputStream(this.propertiesFilePath)){
            smtpProps.load(input);
            System.out.println("Success!");
        }catch(IOException e){
            throw new RuntimeException("Failed to load SMTP config file", e );
        }
    }


    public void sendEmail(String to, String subject, String content){
        String username = smtpProps.getProperty("mail.username");
        String password = smtpProps.getProperty("mail.appPassword");

        Properties props = new Properties();
        props.put("mail.smtp.auth", smtpProps.getProperty("mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", smtpProps.getProperty("mail.smtp.starttls.enable"));
        props.put("mail.smtp.host", smtpProps.getProperty("mail.smtp.host"));
        props.put("mail.smtp.port", smtpProps.getProperty("mail.smtp.port"));

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try{
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
            System.out.println("Email Sent to " + to);

        }catch (MessagingException e){
            System.err.println("Failed to send email " + e.getMessage());
            e.printStackTrace();
        }
    }

}
