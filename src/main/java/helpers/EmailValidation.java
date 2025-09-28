package helpers;

import dao.UserDAO;

import java.util.regex.Pattern;

public class EmailValidation {
	
	public static boolean isValidEmail(String email) {
		String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
		if (Pattern.matches(emailPattern, email)) {
			return true;
		}else{
			System.out.println("Email format is invalid");
			return false;
		}
	}

	public static boolean doesEmailExist(String email){
		return UserDAO.getUserByEmail(email) != null;
	}

}
