package dao;
import java.sql.SQLException;
/** Preserve the cause for server logs; never send SQL or driver messages to clients. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(SQLException cause) { super("Database operation failed", cause); }
    public boolean isConstraintViolation() { return ((SQLException)getCause()).getErrorCode() == 19; }
}
