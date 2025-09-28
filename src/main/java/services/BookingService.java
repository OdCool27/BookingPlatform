package services;

import dao.BookingDAO;
import dao.PropertyDAO;
import helpers.EmailSender;
import models.Booking;
import models.Guest;
import models.Property;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class BookingService {
	//private BookingDAO bookingDAO
	

	//CHECKS IF BOOKING DATE IS AVAILABLE
	public List<Property> listAvailableProperties(LocalDate startDate, LocalDate endDate) {
		List<Booking> bookingList = BookingDAO.retrieveAllBookings();
		List<Property> propertyList = PropertyDAO.retrieveAllProperties();
		List<String> unavailablePropertyID = new ArrayList<>();

		if (!bookingList.isEmpty()) {

			for (Booking book : bookingList) {
				if (!(book.getArrivalDate().isAfter(endDate) || book.getDepartureDate().isBefore(startDate)) && book.getStatus().equals(Booking.Status.CONFIRMED)) {
					unavailablePropertyID.add(book.getPropertyID());
				}
			}

			return propertyList.stream()
					.filter(prop -> !unavailablePropertyID.contains(prop.getPropertyID()))
					.collect(Collectors.toList());
		}
		return propertyList;
	}

	
	//CREATES A BOOKING
	public boolean confirmBooking(Booking book, Guest g, Property property) {
		List<Property> availableProperties = listAvailableProperties(book.getArrivalDate(), book.getDepartureDate());//I need to adjust to return available properties

		Property bookedProperty = PropertyDAO.retrievePropertyByPropertyID(property.getPropertyID());

		if(availableProperties.contains(bookedProperty)){
			BookingDAO.saveBooking(book);
			EmailSender e = new EmailSender();
			e.sendBookingConfirmationEmail(g.getEmail(), book, property);
			return true;

		}else{
			System.out.println("Property is not available");
			return false;
		}
	}
	
	
	
	
	//MODIFY BOOKING - Implement code to check if it is within modification period
	
	//CANCEL BOOKING -  Implement code to check if it is within cancellation period

}
