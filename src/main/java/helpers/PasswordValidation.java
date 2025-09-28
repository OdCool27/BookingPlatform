package helpers;

import java.util.regex.Pattern;

public class PasswordValidation {
	
	
	public static boolean isValidPassword(String password) {
		String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$";
		return Pattern.matches(passwordPattern, password);
	}
	
	
	public static boolean validatePasswordField(String password) {
		
		if (password.length()<8) {
			System.err.println("The password entered is too short. Try again");
			return false;
		}
		
		if (isValidPassword(password)) {
			return true;
		}
		
		System.err.println("Password should:\n -Be atleast 8 characters long\n - Contain at least one common letter\n - Contain at least one capital letter\n - Contain at least one symbol\n - Contain at least number");
		return false;
	}

}
