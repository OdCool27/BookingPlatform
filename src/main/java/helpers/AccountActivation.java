package helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import models.User;
import services.EmailService;

public class AccountActivation {
	
	//PROMPTS ACTIVATION
	public static void promptActivation(User user) {
		user.setActivationCode(assignActivationCode());
		user.setActivationCodeExpiry(assignActivationCodeExpiry());
		EmailSender e = new EmailSender();
		e.sendActivationEmail(user.getEmail(), user.getActivationCode());
		System.out.println("The activation code has been sent to the email address " + user.getEmail() + " and expires " + user.getActivationCodeExpiry().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));
	}
	
	//SET ACTIVATION CODE
	public static String assignActivationCode() {
		Random rand = new Random();
		int code = 100000 + rand.nextInt(900000);
		return Integer.toString(code);
	}
	
	
	//SET ACTIVATION EXPIRY
	public static LocalDateTime assignActivationCodeExpiry() {
		LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);
		return expiry;
	}
	
	
	//ACTIVATES USER ACCOUNT
	public static boolean activateAccount(String enteredCode, User user) {
		LocalDateTime now = LocalDateTime.now();
		
		if (user.getActivationCode() == null || now.isAfter( user.getActivationCodeExpiry())) {
			System.out.println ("Activation Code Has Expired!");
			return false;
		}
		
		if (user.getActivationCode().equals(enteredCode)) {
			user.setActivated(true);
			user.setActivationCode(null);
			user.setActivationCodeExpiry(null);
			System.out.println("Account Activated Successfully!");
			return true;
		}
		
		System.out.println("Incorrect Activation Code");
		return false;
	}
	
	

}
