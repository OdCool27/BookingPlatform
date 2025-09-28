package dao;

import helpers.DBUtil;
import models.Employee;
import models.User;
import org.apache.juli.logging.Log;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static app.Main.LOGGER;

public class EmployeeDAO {

    public static boolean initializeEmployeeTable(){

        String table = "CREATE TABLE IF NOT EXISTS Employees (" +
                "empID VARCHAR(12) PRIMARY KEY, " +
                "email VARCHAR(40) REFERENCES Users(email), " +
                "accessLevel VARCHAR(10) NOT NULL, " +
                "jobRole VARCHAR(20) NOT NULL, " +
                "hireDate DATE NOT NULL," +
                "terminationDate DATE" +
                ");";

        try(Connection dbConn = DBUtil.getConnection();
            Statement dbStmt = dbConn.createStatement()){

            dbStmt.executeUpdate(table);
            LOGGER.info("Table 'Employee' created or already exists.");
            return true;

        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Failed to Initialize Employee Table.", e);
            return false;
        }

    }


    //<---------------------------------- SAVE METHOD ---------------------------------------------->


    public static boolean saveEmployee(Employee emp){


        if(emp == null){
            LOGGER.log(Level.SEVERE, "Cannot save null Employee Record");
            return false;
        }

        initializeEmployeeTable();

        if(UserDAO.saveUser(emp)) {

            String sql = "INSERT INTO Employees (empID, email, accessLevel, jobRole, hireDate, terminationDate) " +
                    "VALUES(?,?,?,?,?,?);";

            try (Connection dbConn = DBUtil.getConnection();
                 PreparedStatement stmt = dbConn.prepareStatement(sql)) {

                stmt.setString(1, emp.getEmployeeID());
                stmt.setString(2, emp.getEmail());
                stmt.setString(3, emp.getAccess().toString());
                stmt.setString(4, emp.getJobRole().toString());
                stmt.setDate(5, Date.valueOf(emp.getHireDate()));

                if (emp.getTerminationDate() != null) {
                    stmt.setDate(6, Date.valueOf(emp.getTerminationDate()));
                } else {
                    stmt.setNull(6, Types.DATE);
                }

                int rowsInserted = stmt.executeUpdate();
                return rowsInserted > 0;

            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to Save Employee Record", e);
                return false;
            }
        }else{
            return false;
        }
    }


    //<---------------------------------- UPDATE METHOD ---------------------------------------------->


    public static boolean updateEmployee(Employee emp){

        if (emp == null) {
            LOGGER.log(Level.WARNING, "Attempted to update a null Employee.");
            return false;
        }

        if(UserDAO.updateUser(emp)) {

            String update = "UPDATE Employees " +
                    "SET email = ?, " +
                    "accessLevel = ?, " +
                    "jobRole = ?, " +
                    "hireDate = ?, " +
                    "terminationDate = ? " +
                    "WHERE empID = ?;";

            try (Connection dbConn = DBUtil.getConnection();
                 PreparedStatement stmt = dbConn.prepareStatement(update)) {

                stmt.setString(1, emp.getEmail());
                stmt.setString(2, emp.getAccess().toString());
                stmt.setString(3, emp.getJobRole().toString());
                stmt.setDate(4, Date.valueOf(emp.getHireDate()));
                stmt.setDate(5, Date.valueOf(emp.getTerminationDate()));

                stmt.setString(6, emp.getEmployeeID());

                int rowsUpdated = stmt.executeUpdate();
                return rowsUpdated > 0;

            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to Update Employee Record.", e);
                return false;
            }
        }else{
            LOGGER.log(Level.SEVERE, "Failed to Update Employee Record.");
            return false;
        }
    }

    //<------------------------------------------- RETRIEVE EMPLOYEE -------------------------------------------------->

    public static Employee retrieveEmployeeByID(String empID){

        String sql = "SELECT * FROM Employees WHERE empID = ?;";

        try(Connection dbConn = DBUtil.getConnection();
        PreparedStatement stmt = dbConn.prepareStatement(sql)){

            stmt.setString(1, empID);

            try(ResultSet rs = stmt.executeQuery()){

                if(rs.next()){
                    String employeeID = rs.getString("empID");
                    String empEmail = rs.getString("email");
                    Employee.AccessLevel access = Employee.AccessLevel.valueOf(rs.getString("accessLevel"));
                    Employee.Role jobRole = Employee.Role.valueOf(rs.getString("jobRole"));
                    LocalDate hireDate = rs.getDate("hireDate").toLocalDate();
                    LocalDate terminationDate = null;
                    Date termSqlDate = rs.getDate("terminationDate");

                    if (termSqlDate != null){
                        terminationDate = termSqlDate.toLocalDate();
                    }

                    User u = UserDAO.getUserByEmail(empEmail);

                    Employee emp =  new Employee(u, employeeID, access, jobRole, hireDate, terminationDate);

                    return emp;
                }
                return null;
            }
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Failed to retrieve Employee Record", e);
            return null;
        }
    }



    public static Employee retrieveEmployeeByEmail(String email){

        String sql = "SELECT * FROM Employees WHERE email = ?;";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(sql)){

            stmt.setString(1, email);

            try(ResultSet rs = stmt.executeQuery()){

                if(rs.next()){
                    String employeeID = rs.getString("empID");
                    String empEmail = rs.getString("email");
                    Employee.AccessLevel access = Employee.AccessLevel.valueOf(rs.getString("accessLevel"));
                    Employee.Role jobRole = Employee.Role.valueOf(rs.getString("jobRole"));
                    LocalDate hireDate = rs.getDate("hireDate").toLocalDate();
                    LocalDate terminationDate = rs.getDate("terminationDate").toLocalDate();

                    User u = UserDAO.getUserByEmail(empEmail);

                    Employee emp =  new Employee(u, employeeID, access, jobRole, hireDate, terminationDate);

                    return emp;
                }
                return null;
            }
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Failed to retrieve Employee Record", e);
            return null;
        }
    }




    public static List<Employee> retrieveAllEmployees(){

        String sql = "SELECT * FROM Employees;";

        try(Connection dbConn = DBUtil.getConnection();
        PreparedStatement stmt = dbConn.prepareStatement(sql)){

            List<Employee> employeeList = new ArrayList<>();

            try(ResultSet rs = stmt.executeQuery()){

                while(rs.next()) {

                    String empID = rs.getString("empID");
                    String empEmail = rs.getString("email");
                    Employee.AccessLevel accessLevel = Employee.AccessLevel.valueOf(rs.getString("accessLevel"));
                    Employee.Role jobRole = Employee.Role.valueOf(rs.getString("jobRole"));
                    LocalDate hireDate = rs.getDate("hireDate").toLocalDate();
                    LocalDate terminationDate = null;
                    Date termSqlDate = rs.getDate("terminationDate");

                    if(termSqlDate != null){
                        terminationDate = termSqlDate.toLocalDate();
                    }

                    User u = UserDAO.getUserByEmail(empEmail);

                    if (u==null){
                        LOGGER.log(Level.SEVERE, "User not found with that email address");
                        continue;
                    }

                    Employee emp = new Employee(u, empID, accessLevel, jobRole, hireDate, terminationDate);

                    employeeList.add(emp);

                }

                return employeeList;
            }

        }catch(SQLException e){
            LOGGER.log(Level.SEVERE, "Failed to Retrieve Employee Records", e);
            return Collections.emptyList();
        }
    }

    //<------------------------------------------ DELETE METHODS ------------------------------------------------------>

        public static boolean deleteEmployeeByEmail(String email){

        String sql = "DELETE FROM Employees WHERE email = ?;";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(sql)){

            stmt.setString(1, email);

            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted>0){
                if (UserDAO.deleteUserByEmail(email)){
                    return true;
                }else{
                    LOGGER.log(Level.SEVERE, "Employee Records deleted successfully; failed to delete user records.");
                    return false;
                }
            }else{
                LOGGER.log(Level.SEVERE, "Employee Record not found.");
                return false;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete Employee Record", e);
            return false;
        }
    }

}
