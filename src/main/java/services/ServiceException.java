package services;

/** Business failures are independent of HTTP; the router maps reasons to status codes. */
public class ServiceException extends RuntimeException {
    public enum Reason { INVALID, UNAUTHENTICATED, FORBIDDEN, NOT_FOUND, CONFLICT, UNAVAILABLE }
    private final Reason reason;
    public ServiceException(Reason reason, String message) { super(message); this.reason = reason; }
    public Reason reason() { return reason; }
}
