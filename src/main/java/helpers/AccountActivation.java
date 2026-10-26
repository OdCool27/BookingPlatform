package helpers;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import models.User;

/** Existing ten-minute email-code activation, without console interaction. */
public final class AccountActivation {
    private AccountActivation() {}
    public static String assignActivationCode() { return Integer.toString(100000 + new SecureRandom().nextInt(900000)); }
    public static LocalDateTime assignActivationCodeExpiry() { return LocalDateTime.now().plusMinutes(10); }
    public static boolean activateAccount(String enteredCode,User user) {
        if(user.getActivationCode() == null || user.getActivationCodeExpiry() == null ||
                !LocalDateTime.now().isBefore(user.getActivationCodeExpiry())) return false;
        if(!user.getActivationCode().equals(enteredCode)) return false;
        user.setActivated(true); user.setActivationCode(null); user.setActivationCodeExpiry(null);
        return true;
    }
}
