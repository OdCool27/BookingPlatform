package services;

import dao.*;
import helpers.*;
import models.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import static services.ServiceException.Reason.*;

public class AuthService {
    private final EmailSender email;
    private static final String DUMMY_HASH = PasswordSecurity.hashPassword("InvalidPassword1!");
    public AuthService(EmailSender email) { this.email = email; }

    public User register(User user, String password) {
        Validation.text(user.getFirstName(),"firstName",30);
        Validation.text(user.getLastName(),"lastName",30);
        Validation.text(user.getContactNum(),"contactNum",30);
        validateCredentials(user.getEmail(),password);
        if(!PasswordValidation.isValidPassword(password))
            throw new ServiceException(INVALID,"Password needs 8 characters, uppercase, lowercase, number and symbol");
        if(!(user instanceof Guest) && !(user instanceof PropertyOwner))
            throw new ServiceException(INVALID,"accountType must be GUEST or OWNER");
        if(user instanceof Guest g) {
            Validation.text(g.getNationality(),"nationality",80);
            if(g.getDob() == null || !g.getDob().isBefore(LocalDate.now()))
                throw new ServiceException(INVALID,"dob must be a past date");
            if(g.getPreferences() == null || g.getPreferences().size() > 50 ||
                    g.getPreferences().stream().anyMatch(p -> p == null || p.length() > 200))
                throw new ServiceException(INVALID,"preferences must contain at most 50 strings of 200 characters");
        }
        User existing = UserDAO.getUserByEmail(user.getEmail());
        if(existing != null) {
            // The CLI could prompt activation again. Retrying registration with the original password
            // resends a fresh code for a pending account; it cannot overwrite profile or role data.
            if(existing.isActivated() || !PasswordSecurity.verifyPassword(password,existing.getPasswordHash()))
                throw new ServiceException(CONFLICT,"Account already exists");
            assignActivation(existing);
            UserDAO.updateUser(existing);
            email.sendActivationEmail(existing.getEmail(),existing.getActivationCode());
            return existing;
        }
        user.setPasswordHash(PasswordSecurity.hashPassword(password));
        user.setActivated(false); assignActivation(user);
        if(user instanceof Guest g) {
            g.setGuestID("G-" + UUID.randomUUID()); GuestDAO.saveGuest(g);
        } else if(user instanceof PropertyOwner o) {
            o.setOwnerID("PO-" + UUID.randomUUID()); PropertyOwnerDAO.savePropertyOwner(o);
        }
        // Persist before delivery so a successfully delivered code is always usable.
        // If SMTP fails, the pending account remains and the same registration can be retried.
        email.sendActivationEmail(user.getEmail(),user.getActivationCode());
        return user;
    }

    private void assignActivation(User u) {
        u.setActivationCode(AccountActivation.assignActivationCode());
        u.setActivationCodeExpiry(AccountActivation.assignActivationCodeExpiry());
    }
    private static void validateCredentials(String email, String password) {
        Validation.text(email,"email",80); Validation.text(password,"password",72);
        if(!EmailValidation.isValidEmail(email) || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ServiceException(INVALID,"Invalid email or password exceeds BCrypt's 72-byte limit");
    }
    public User authenticate(String email, String password) {
        if(email == null || password == null || email.length() > 80 || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ServiceException(UNAUTHENTICATED,"Invalid credentials");
        User u = UserDAO.getUserByEmail(email);
        boolean matches = PasswordSecurity.verifyPassword(password,u == null ? DUMMY_HASH : u.getPasswordHash());
        if(u == null || !matches) throw new ServiceException(UNAUTHENTICATED,"Invalid credentials");
        if(!u.isActivated()) throw new ServiceException(FORBIDDEN,"Account must be activated");
        Guest guest = GuestDAO.getGuestByEmail(email);
        if(guest != null) return guest;
        PropertyOwner owner = PropertyOwnerDAO.getOwnerByEmail(email);
        if(owner != null) return owner;
        // Admin is an existing employee role. Authentication does not grant guest/owner privileges.
        Employee employee = EmployeeDAO.retrieveEmployeeByEmail(email);
        if(employee != null && employee.getJobRole() == Employee.Role.ADMIN && employee.getTerminationDate() == null)
            return employee;
        throw new ServiceException(FORBIDDEN,"Account type is not supported by V1");
    }
    public void activate(String emailAddress, String password, String code) {
        // Requiring the registration password prevents a six-digit code alone from granting access.
        validateCredentials(emailAddress,password);
        Validation.text(code,"code",6);
        User u = UserDAO.getUserByEmail(emailAddress);
        if(u == null || !PasswordSecurity.verifyPassword(password,u.getPasswordHash()))
            throw new ServiceException(UNAUTHENTICATED,"Invalid credentials");
        if(u.isActivated()) return;
        if(!AccountActivation.activateAccount(code,u)) throw new ServiceException(INVALID,"Invalid or expired activation code");
        UserDAO.updateUser(u);
    }
    public static PropertyOwner owner(User u) {
        requireActive(u);
        if(u instanceof PropertyOwner owner) return owner;
        throw new ServiceException(FORBIDDEN,"Owner account required");
    }
    public static Guest guest(User u) {
        requireActive(u);
        if(u instanceof Guest guest) return guest;
        throw new ServiceException(FORBIDDEN,"Guest account required");
    }
    public static void requireActive(User u) {
        if(u == null) throw new ServiceException(UNAUTHENTICATED,"Authentication required");
        if(!u.isActivated()) throw new ServiceException(FORBIDDEN,"Account must be activated");
    }
}
