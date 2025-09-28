package models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Guest extends User{
	private String guestID;
	private String nationality;
	private int points;
	private LocalDate dob;
	private PaymentDetails payment;
	private List<String> preferences;
	private List<Booking> bookingHistory;
	private List<Transaction> transactionList;
	
	
	public Guest() {
		super();
		this.guestID = "";
		this.nationality = "";
		this.points = 0;
		this.dob = null;
		this.payment = new PaymentDetails();
		this.preferences = new ArrayList<>();
		this.bookingHistory = new ArrayList<>();
		this.transactionList = new ArrayList<>();
	}
	
	
	public Guest(User user, String guestID, String nationality, int points,
				 LocalDate dob, PaymentDetails paymentDetails, List<String> preferences, List<Booking> bookingHistory,
				 List<Transaction> transactionList) {
		super(user);
		this.guestID = guestID;
		this.nationality = nationality;
		this.points = points;
		this.dob = dob;
		this.payment = paymentDetails;
		this.preferences = preferences;
		this.bookingHistory = bookingHistory;
		this.transactionList = transactionList;
	}
	
	
	public Guest(Guest guest) {
		super(guest);
		this.guestID = guest.guestID;
		this.nationality = guest.nationality;
		this.points = guest.points;
		this.dob = guest.dob;
		this.payment = guest.payment;
		this.preferences = guest.preferences;
		this.bookingHistory = guest.bookingHistory;
		this.transactionList = guest.transactionList;
	}


	//ACCESSORS
	public String getGuestID() {
		return guestID;
	}

	public String getNationality() {
		return nationality;
	}

	public int getPoints() {
		return points;
	}

	public LocalDate getDob() {
		return dob;
	}

	public PaymentDetails getPaymentDetails() {
		return payment;
	}
	
	public List<String> getPreferences(){
		return preferences;
	}

	public List<Booking> getBookingHistory() {
		return bookingHistory;
	}

	public List<Transaction> getTransactionList(){return transactionList;}


	
	//MUTATORS
	public void setGuestID(String guestID) {
		this.guestID = guestID;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	public void setChargeDetails(PaymentDetails paymentDetails) {
		this.payment = paymentDetails;
	}
	
	public void setPreferences(List<String> preferences) {
		this.preferences = preferences;
	}

	public void setBookingHistory(List<Booking> bookingHistory) {
		this.bookingHistory = bookingHistory;
	}

	public void setTransactionList(List<Transaction> transactionList){
		this.transactionList = transactionList;
	}
	


	//TO STRING METHOD
	@Override
	public String toString() {
		String output = super.toString();
		output += "Guest ID: " + guestID + "\n";
		output += "Nationality: " + nationality + "\n";
		output += "Preferences: " + String.join(", ", preferences);
		output += "\nDate of Birth: " + dob + "\n";
		output += "Points: " + points + "\n";
		output += "Total Number of Bookings: " + bookingHistory.size() + "\n";
		output += "Total Number of Transactions: " + transactionList.size();
		return output;
	}

	//EQUALS METHOD
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		Guest guest = (Guest) o;
		return points == guest.points && Objects.equals(guestID, guest.guestID) &&
				Objects.equals(nationality, guest.nationality) && Objects.equals(dob, guest.dob) &&
				Objects.equals(payment, guest.payment) && Objects.equals(preferences, guest.preferences) &&
				Objects.equals(bookingHistory, guest.bookingHistory) &&
				Objects.equals(transactionList, guest.transactionList);
	}

	//HASH METHOD
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), guestID, nationality, points, dob, payment, preferences, bookingHistory, transactionList);
	}

	//DISPLAY METHOD
	public void displayGuest() {
		System.out.println(this);
	}

}
