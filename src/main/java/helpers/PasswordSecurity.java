package helpers;

public class PasswordSecurity {
	
	
	//USED WHERE PASSWORD IS TO BE DISPLAYED
	public static String maskPassword(String password) {
		
		StringBuilder maskedPassword = new StringBuilder();
		
		for (int i = 0; i < password.length(); i++) {
			
			maskedPassword.append("*");
		}
		return maskedPassword.toString();
	}
	
	
	// Hash a plain password
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    // Verify input password with stored hashed password
    public static boolean verifyPassword(String inputPassword, String storedHash) {
        return BCrypt.checkpw(inputPassword, storedHash);
    }

}
