package services;
import java.time.LocalDate;
import static services.ServiceException.Reason.INVALID;
public final class Validation {
    private Validation() {}
    public static String text(String value, String name, int max) {
        if(value == null || value.isBlank() || value.length() > max)
            throw new ServiceException(INVALID,name + " is required and must be at most " + max + " characters");
        return value;
    }
    public static void dates(LocalDate start, LocalDate end) {
        if(start == null || end == null || !end.isAfter(start))
            throw new ServiceException(INVALID,"departureDate must be after arrivalDate");
    }
}
