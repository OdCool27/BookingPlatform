package app;

import dao.*;
import helpers.EmailSender;
import helpers.PasswordSecurity;
import models.*;
import helpers.idGenerators;
import org.junit.jupiter.api.Test;
import services.BookingService;
import services.EmailService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UnitTest {
    public static void main(String[] args) {

        PropertyDAO.initializePropertiesTable();
        BookingDAO.initializeBookingTable();

        /*
        //TESTING EMPLOYEE DAO
        Employee emp = new Employee(new User(), "E-ODCO-00001", Employee.AccessLevel.SUPERADMIN, Employee.Role.MANAGER, LocalDate.now(), null);
        emp.setEmail("odanecollins@gmail.com");

        //SAVE
        //EmployeeDAO.saveEmployee(emp);

        //UPDATE
        emp.setTerminationDate(LocalDate.now().plusDays(10));
        EmployeeDAO.updateEmployee(emp);

        //RETRIEVE
        Employee retrievedEmp = EmployeeDAO.retrieveEmployeeByID("E-ODCO-00001");
        System.out.println(retrievedEmp);

        //DELETE
        System.out.println(
                EmployeeDAO.deleteEmployeeByEmail("odanecollins@gmail.com")
        );
        */

//        User u = new User();
//        u.setEmail("jackthompson@gmail.com");
//        u.setPasswordHash(PasswordSecurity.hashPassword("Thompson@2025"));
//        u.setUserID(100);
//        u.setFirstName("Jack");
//        u.setLastName("Thompson");
//
//        u.displayUser();

    }
}
