package services;
@FunctionalInterface
public interface MailSender { void sendEmail(String to, String subject, String content); }
