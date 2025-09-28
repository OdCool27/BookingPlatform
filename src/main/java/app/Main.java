package app;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import dao.BookingDAO;
import dao.GuestDAO;
import dao.PropertyDAO;
import dao.PropertyOwnerDAO;
import helpers.*;
import models.*;
import services.BookingService;
import services.PropertyManagementService;

//TESTING OF MULTITHREADING

public class Main{

    //LOGGER
    public static final Logger LOGGER = Logger.getLogger(app.Main.class.getName());

    public static void main(String[] args) {
        start();
    }




    //VAPFUMI CONNECT START
    public static void start() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("============================================================");
        System.out.println("                     VAPFUMI CONNECT                        ");
        System.out.println("============================================================\n");
        System.out.println("Select an option: ");
        System.out.println("1. Sign up");
        System.out.println("2. Log in");
        System.out.println("0. Exit\n");

        int option = scanner.nextInt();

        switch (option) {
            case 1:
                signUp(scanner);
                break;

            case 2:
                logIn(scanner);
                break;

            case 0:
                System.exit(option);

            default:
                System.out.println("Invalid Input");
        }

        scanner.close();
    }

    //GENERIC SIGN UP
    public static void signUp(Scanner scanner) {

        User user = new User();

        //Account Type
        System.out.println("Select Account type:\n1.Guest \n2.Property Owner ");
        int accountType = scanner.nextInt();


        if (accountType == 1) {
            user = new Guest();
        }

        if (accountType == 2) {
            user = new PropertyOwner();
        }


        //Input User Related Data
        System.out.print("Enter First Name: ");
        String fName = scanner.next();

        System.out.print("Enter Last Name: ");
        String lName = scanner.next();

        String gender;
        do {
            System.out.println("Select your gender: \nM for Male\nF for Female\nN for Not Specified");
            gender = scanner.next();

            switch (gender.toUpperCase()) {
                case "M":
                    user.setGender(User.Gender.MALE);
                    break;

                case "F":
                    user.setGender(User.Gender.FEMALE);
                    break;

                case "N":
                    user.setGender(User.Gender.NOT_SPECIFIED);
                    break;

                default:
                    System.out.println("Invalid Selection. Try again \n");
            }

        } while (!gender.equalsIgnoreCase("m") && !gender.equalsIgnoreCase("n") && !gender.equalsIgnoreCase("f"));

        System.out.print("Enter Contact Number: ");
        String cNum = scanner.next();

        String email;

        while (true) {
            System.out.print("Enter email: ");
            email = scanner.next();

            if (EmailValidation.isValidEmail(email) && !EmailValidation.doesEmailExist(email)) {
                break;
            } else {
                System.out.println("Email invalid or already exists.");
            }

        }

        String password;
        String passwordHash;

        //Enforces Password Validation
        while (true) {
            String password1;
            String password2;

            while (true) {
                System.out.print("Enter password: ");
                System.out.flush();
                password1 = scanner.next();
                if (PasswordValidation.validatePasswordField(password1)) {
                    break;
                }
            }

            while (true) {
                System.out.print("Re-enter password: ");
                System.out.flush();
                password2 = scanner.next();
                if (PasswordValidation.validatePasswordField(password2)) {
                    break;
                }
            }


            if (password1.equals(password2)) {
                password = password1;
                passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
                break;
            } else {
                System.out.println("The passwords entered do not match. Try again.\n");
            }
        }


        //Assigns values to User object attributes
        user.setFirstName(fName);
        user.setLastName(lName);
        user.setContactNum(cNum);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);

        while (true) {
            //Sends a code to push activation process
            AccountActivation.promptActivation(user);

            System.out.println("\n\nEnter activation code that was sent to your email:");
            String enteredCode = scanner.next();

            //Checks if activation code is correct and is not expired
            if (AccountActivation.activateAccount(enteredCode, user)) {
                break;
            }
        }

        if (user instanceof PropertyOwner pOwner) {
            completeOwnerDetails(scanner, pOwner);
        }

        if (user instanceof Guest guest) {
            completeGuestDetails(scanner, guest);
        }

        scanner.close();

    }

    //CONTINUES SIGN UP PROCESS FOR GUESTS
    public static void completeGuestDetails(Scanner scanner, User user) {
        Guest guest = (Guest) user;

        //Continue with Collecting Guest Data
        guest.setGuestID(idGenerators.guestIDGenerator(guest.getFirstName(), guest.getLastName()));

        System.out.print("Enter nationality: ");
        guest.setNationality(scanner.next());

        System.out.print("Enter DOB (YYYY MM DD): ");
        guest.setDob(LocalDate.of(scanner.nextInt(), scanner.nextInt(), scanner.nextInt()));

        List<String> pref = new ArrayList<>();

        System.out.println("Enter your stay preferences one at a time (Enter 'end' when finished):");

        while (true) {
            String input = scanner.nextLine().trim();

            if (!input.equals("end")) {
                pref.add(input);
            } else {
                break;
            }
        }

        guest.setPreferences(pref);
        GuestDAO.saveGuest(guest);//Saves guest details to database


        //DISPLAYS GUEST ONCE ALL DATA IS COLLECTED
        if (guest.isActivated()) {
            showGuestMenu(scanner, guest);
        } else {
            System.out.println("Account is not activated, therefore will not proceed to home page.");
            return;
        }

        scanner.close();
    }

    //CONTINUES SIGN UP PROCESS FOR PROPERTY OWNERS
    public static void completeOwnerDetails(Scanner scanner, User user){

        PropertyOwner pO = (PropertyOwner) user;
        pO.setOwnerID(idGenerators.ownerIDGenerator(pO.getFirstName(), pO.getLastName()));

        PropertyOwnerDAO.savePropertyOwner(pO);

        //DISPLAYS GUEST ONCE ALL DATA IS COLLECTED
        if (pO.isActivated()) {
            showOwnerMenu(scanner, pO);
        } else {
            System.out.println("Account is not activated, therefore will not proceed to home page.");
        }

    }

    //GENERIC LOGIN
    public static void logIn(Scanner scanner) {

        System.out.println("Choose account type:\n1.Guest\n2.Property Owner");
        int option = scanner.nextInt();

        switch (option) {
            case 1:
                guestLogin(scanner);
                break;

            case 2:
                propertyOwnerLogin(scanner);
                break;

            default:
                System.out.println("Invalid Input");
                logIn(scanner);
        }
    }

    //LOGIN PROCESS FOR GUESTS - (PERSISTENCE REQUIRED)
    public static void guestLogin(Scanner scanner) {
        while (true) {

            System.out.print("Enter email: ");
            String email = scanner.next();

            System.out.print("Enter password: ");
            String password = scanner.next();

            Guest guest = GuestDAO.getGuestByEmail(email);//Should be updated with GuestDAO

            if (guest == null) {
                System.out.println("User not found");
            } else {
                if (BCrypt.checkpw(password, guest.getPasswordHash())) {//Content will be replace with copy constructor once GuestDAO is implemented
                    guest.setEmail(guest.getEmail());
                    guest.setPasswordHash(guest.getPasswordHash());
                    guest.setFirstName(guest.getFirstName());
                    guest.setLastName(guest.getLastName());
                    guest.setContactNum(guest.getContactNum());
                    guest.setProfilePictureURL(guest.getProfilePictureURL());
                    guest.setActivated(guest.isActivated());
                    guest.setBookingHistory(new ArrayList<>());

                    showGuestMenu(scanner, guest);
                    break;
                }
            }

        }

    }

    //LOGIN PROCESS FOR PROPERTY OWNERS - (PERSISTENCE REQUIRED)
    public static void propertyOwnerLogin(Scanner scanner) {
        while (true) {

            System.out.print("Enter email: ");
            String email = scanner.next();

            System.out.print("Enter password: ");
            String password = scanner.next();

            PropertyOwner user = PropertyOwnerDAO.getOwnerByEmail(email);//Should be updated with GuestDAO

            if (user == null) {
                System.out.println("User not found");
            } else {
                if (BCrypt.checkpw(password, user.getPasswordHash())) {//Content will be replace with copy constructor once GuestDAO is implemented
                    showOwnerMenu(scanner, user);
                    break;
                }
            }

        }
    }

    //SHOW GUEST MENU
    public static void showGuestMenu(Scanner scanner, Guest guest) {
        System.out.println("============================================================");
        System.out.println("            WELCOME " + guest.getFirstName().toUpperCase() + "              ");
        System.out.println("============================================================\n");
        guest.displayGuest();

        System.out.println("\n\nSelect an option:\n0.Logout\n1.Book a Rental\n2.Manage my Booking\n3.View Booking History\n4.View available services\n");
        int option = scanner.nextInt();

        switch (option) {
            case 0:
                System.out.println("Logging out...");
                System.exit(0);
                break;
            case 1:
                guestBooking(scanner, guest);
                break;

            case 2:
                manageGuestBooking(scanner, guest);
                break;

            case 3:
                viewBookingHistory(scanner, guest);
                break;

            case 4:
                //viewAvailableServices(scanner, guest);
                break;


            default:
                System.out.println("Invalid input");
                showGuestMenu(scanner, guest);
        }
    }

    //MANAGE BOOKINGS
    private static void manageGuestBooking(Scanner scanner, Guest guest) {
        System.out.println("1. Modify Booking");
        System.out.println("2. Cancel Booking");
        int option = scanner.nextInt();
        scanner.nextLine();

        List<Booking> bookingList = BookingDAO.retrieveBookingByGuestID(guest.getGuestID());

        if (option == 1){
            System.out.println("Select the booking you'd like to modify\n");

            assert bookingList != null;
            for (Booking book : bookingList) {
                System.out.println(bookingList.indexOf(book) + 1 + ". " + PropertyDAO.retrievePropertyByPropertyID(book.getPropertyID()).getPropertyName() + " : " + book.getArrivalDate() + " - " + book.getDepartureDate());
            }

            int selection = scanner.nextInt();
            scanner.nextLine();

            Booking b = new Booking();

            if (selection <= bookingList.size() && selection > 0){
                b = bookingList.get(selection-1);
            }

            System.out.println("1. Change Dates");
            selection = scanner.nextInt();
            scanner.nextLine();

            if (selection == 1){
                System.out.println("Enter arrival date: (YYYY MM DD)");
                LocalDate newArrival = LocalDate.of(scanner.nextInt(), scanner.nextInt(), scanner.nextInt());

                System.out.println("Enter departure date: (YYYY MM DD)");
                LocalDate newDeparture = LocalDate.of(scanner.nextInt(), scanner.nextInt(), scanner.nextInt());

                b.setArrivalDate(newArrival);
                b.setDepartureDate(newDeparture);
                b.calculateTotalCharge();
            }

            b.displayBooking();

            System.out.println("Would you like to confirm booking? Y/N");
            String choice = scanner.nextLine();scanner.nextLine();

            if (choice.equalsIgnoreCase("y")){
                BookingDAO.updateBooking(b);
            }else{
                System.out.println("Booking adjustment cancelled\n");
            }


        }

        showGuestMenu(scanner, guest);

    }


    //VIEW BOOKING HISTORY
    public static void viewBookingHistory(Scanner scanner, Guest guest){
        List<Booking> bookingList = BookingDAO.retrieveBookingByGuestID(guest.getGuestID());
        assert bookingList != null;
        System.out.println("ALL MY BOOKINGS\n===============================================\n");
        for (Booking book : bookingList) {
            System.out.println(bookingList.indexOf(book) + 1 + ". " + PropertyDAO.retrievePropertyByPropertyID(book.getPropertyID()).getPropertyName() + " : " + book.getArrivalDate() + " - " + book.getDepartureDate() + " - " + book.getStatus());
        }
        showGuestMenu(scanner, guest);
    }

    //SHOW OWNER MENU
    public static void showOwnerMenu(Scanner scanner, PropertyOwner pO){
        pO.displayPropertyOwner();

        System.out.println("0. Logout");
        System.out.println("1. Manage Properties");
        System.out.println("2. View Reports");

        int option = scanner.nextInt();

        switch(option){
            case 0:
                System.out.println("Logging out...");
                System.exit(0);
                break;
            case 1:
                manageProperties(scanner, pO);
                break;

            case 2:
                //viewReports(scanner);
                break;
        }
    }

    //MANAGE PROPERTIES
    private static void manageProperties(Scanner scanner, PropertyOwner pO) {
        System.out.println("1. Add Property");
        System.out.println("2. Update Property");
        System.out.println("3. Delete Property");
        System.out.println("4. View My Properties");

        int option = scanner.nextInt();
        scanner.nextLine();

        PropertyManagementService pms = new PropertyManagementService();
        Property p;

        switch(option){
            case 1:
                //COLLECT PROPERTY DETAILS HERE
                System.out.println("Property Name: ");
                String propertyName = scanner.nextLine();

                System.out.println("Description: ");
                String description = scanner.nextLine();

                System.out.println("Land Size: ");
                double landSize =  Double.parseDouble(scanner.nextLine());

                System.out.println("House Size: ");
                double houseSize =  Double.parseDouble(scanner.nextLine());

                System.out.println("Base Rate: ");
                BigDecimal baseRate = new BigDecimal(scanner.nextLine());

                System.out.println("Street: ");
                String street = scanner.nextLine();

                System.out.println("Suite: ");
                String suite = scanner.nextLine();

                System.out.println("City: ");
                String city = scanner.nextLine();

                System.out.println("State: ");
                String state = scanner.nextLine();

                System.out.println("Postal Code: ");
                String postalCode = scanner.nextLine();

                System.out.println("Country: ");
                String country = scanner.nextLine();

                System.out.println("Latitude: ");
                double latitude = scanner.nextDouble();

                System.out.println("Longitude: ");
                double longitude = scanner.nextDouble();


                Address propertyAddress = new Address(street, suite, city, state, postalCode, country);
                p = new Property(idGenerators.propertyIDGenerator(propertyAddress), pO.getOwnerID(), propertyName, description,
                        propertyAddress, latitude, longitude, houseSize, landSize, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                        baseRate, new ArrayList<>(), new PropertyVerification());//INITIALIZE PROPERTY OBJECT
                pms.addProperty(p);//ACCEPTS PROPERTY OBJECT AND SAVES TO DB
                showOwnerMenu(scanner, pO);
                break;

            case 2:
                int count2 =1;
                List<Property> propList = pms.viewMyProperties(pO.getOwnerID());
                for(Property property:propList){
                    System.out.println(count2 + ". " + property.getPropertyName() + " - " + property.getPropertyID());
                    count2++;
                }

                System.out.print("\nEnter the ID of Property You Would like to Update: ");
                String updateID = scanner.nextLine();

                Property updatedProperty = pms.findProperty(updateID);

                //COLLECT PROPERTY DETAILS HERE
                System.out.println("Property Name: ");
                updatedProperty.setPropertyName(scanner.nextLine());

                System.out.println("Description: ");
                updatedProperty.setDescription(scanner.nextLine());

                System.out.println("Land Size: ");
                updatedProperty.setLandSize(Double.parseDouble(scanner.nextLine()));

                System.out.println("House Size: ");
                updatedProperty.setHouseSize(Double.parseDouble(scanner.nextLine()));

                System.out.println("Base Rate: ");
                updatedProperty.setBaseRate(new BigDecimal(scanner.nextLine()));

                System.out.println("Street: ");
                updatedProperty.getAddress().setStreet(scanner.nextLine());

                System.out.println("Suite: ");
                updatedProperty.getAddress().setSuite(scanner.nextLine());

                System.out.println("City: ");
                updatedProperty.getAddress().setCity(scanner.nextLine());

                System.out.println("State: ");
                updatedProperty.getAddress().setState(scanner.nextLine());

                System.out.println("Postal Code: ");
                updatedProperty.getAddress().setPostalCode(scanner.nextLine());

                System.out.println("Country: ");
                updatedProperty.getAddress().setCountry(scanner.nextLine());

                System.out.println("Latitude: ");
                updatedProperty.setLatitude(scanner.nextDouble());

                System.out.println("Longitude: ");
                updatedProperty.setLongitude(scanner.nextDouble());

                pms.updateProperty(updatedProperty);//ACCEPTS PROPERTY OBJECT AND UPDATES DB
                break;

            case 3:
                //RETRIEVE PROPERTY LIST BASED ON PROPERTY OWNER ID
                List<Property> propList3 = pms.viewMyProperties(pO.getOwnerID());//ACCEPTS PROPERTY Owner OBJECT(OR ID) AND RETRIEVES FROM DB
                for(Property property:propList3){
                    System.out.println(property.getPropertyID() + ". " + property.getPropertyName());
                }

                System.out.print("Enter the ID of the property you would like to delete: ");
                String deleteID = scanner.nextLine();

                System.out.println("Are you sure you want to delete property " + deleteID +"? (Y/N)");
                String confirmation = scanner.nextLine();

                if(confirmation.toUpperCase().equals("Y")){
                    pms.deleteProperty(deleteID);//ACCEPTS PROPERTY OBJECT(OR ID) AND DELETES FROM DB
                }else{
                    System.out.println("Deletion Cancelled");
                }

                break;

            case 4:
                //VIEW PROPERTY LIST BASED ON PROPERTY OWNER ID
                int count4 = 1;
                List<Property> propList4 = pms.viewMyProperties(pO.getOwnerID());//ACCEPTS PROPERTY Owner OBJECT(OR ID) AND RETRIEVES FROM DB
                for(Property property:propList4){
                    System.out.println(count4 + ". " + property.getPropertyName() + " - $" + property.getBaseRate() + "/night");
                    count4++;
                }
                break;

            default:

        }

        showOwnerMenu(scanner, pO);

    }

    // ALLOWS GUEST TO BOOK
    public static void guestBooking(Scanner scanner, Guest guest) {

        //TO SIMULATE A PROPERTY LIST
        List<Property> propertyList = PropertyDAO.retrieveAllProperties();

        System.out.println("Select an option from the list: ");

        assert propertyList != null;
        for (Property prop : propertyList) {
            System.out.println(propertyList.indexOf(prop) + 1 + ". " + prop.getPropertyName() + " - " + prop.getBaseRate() + "/night");
        }

        int option = scanner.nextInt();

        System.out.print("Arrival Date (YYYY MM DD): ");
        LocalDate arrDate = LocalDate.of(scanner.nextInt(), scanner.nextInt(), scanner.nextInt());

        System.out.print("Number of Nights: ");
        int numOfNights = scanner.nextInt();

        LocalDate deptDate = arrDate.plusDays(numOfNights);


        Property selectedProperty = propertyList.get(option - 1);

        if (selectedProperty != null) {
            Booking booking = new Booking();
            booking.setBookingID(idGenerators.bookingIDGenerator(0));
            booking.setGuestID(guest.getGuestID());
            booking.setPropertyID(selectedProperty.getPropertyID());
            booking.setArrivalDate(arrDate);
            booking.setDepartureDate(deptDate);
            booking.setBaseCharge(selectedProperty.getBaseRate().multiply(BigDecimal.valueOf(numOfNights)));
            booking.setTotalCharge(booking.calculateTotalCharge());
            booking.setStatus(Booking.Status.CONFIRMED);
            guest.getBookingHistory().add(booking);
            booking.displayBooking();

            System.out.println("\n\nWould you like to confirm this booking? 1 for Yes, 2 for No\n");

            option = scanner.nextInt();

            if (option == 1) {
                BookingService bS = new BookingService();
                bS.confirmBooking(booking, guest, selectedProperty);
            }else{
                System.out.println("Booking cancelled\n");
            }

            showGuestMenu(scanner, guest);



        }


    }

}
