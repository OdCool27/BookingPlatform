package dao;

import java.time.*;

/** SQLite TEXT columns need explicit ISO values. The former JDBC setters wrote epoch milliseconds
 * into these columns; read that legacy representation as well, without rewriting existing data. */
final class SQLiteValues {
    private SQLiteValues() {}
    static LocalDate date(String value) {
        if(value == null) return null;
        if(value.matches("-?[0-9]+")) return new java.sql.Date(Long.parseLong(value)).toLocalDate();
        return LocalDate.parse(value.substring(0,10));
    }
    static LocalDateTime timestamp(String value) {
        if(value == null) return null;
        if(value.matches("-?[0-9]+")) return new java.sql.Timestamp(Long.parseLong(value)).toLocalDateTime();
        return LocalDateTime.parse(value.replace(' ','T'));
    }
}
