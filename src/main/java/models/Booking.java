package models;

import dao.PropertyDAO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Booking {
	private static final double gctRate = 0.15; //ONLY APPLICABLE IF INCOME EXCEEDS 10 MIL ANNUALLY
	private static final BigDecimal tefRate = BigDecimal.valueOf(1); //Tourism Enhancement Fund Tax of 1USD
	private String bookingID;
	private String guestID;
	private String propertyID;
	private LocalDate arrivalDate;
	private LocalDate departureDate;
	private BigDecimal baseCharge;
	private BigDecimal gct;
	private BigDecimal tef;
	private BigDecimal gart;
	private BigDecimal totalCharge;
	private Status status;
	private PaymentStatus payStatus;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	public enum Status{CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, NO_SHOW}
	public enum PaymentStatus{PENDING, PAID, FAILED, REFUNDED}
	
	
	//CONSTRUCTORS
	
	//DEFAULT
	public Booking() {
		this.bookingID = "";
		this.guestID = "";
		this.propertyID = "";
		this.arrivalDate = LocalDate.now();
		this.departureDate = LocalDate.now().plusDays(2);
		this.baseCharge = BigDecimal.valueOf(0);
		this.gct = BigDecimal.valueOf(0);
		this.tef = BigDecimal.valueOf(0);
		this.gart = BigDecimal.valueOf(0);
		this.totalCharge = BigDecimal.valueOf(0);
		this.status = Status.CONFIRMED;
		this.payStatus = PaymentStatus.PENDING;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
	
	
	//PRIMARY
	public Booking(String bookingID, String guestID, String propertyID, LocalDate arrivalDate, LocalDate departureDate,
				   BigDecimal baseCharge, BigDecimal gct, BigDecimal tef, BigDecimal gart, BigDecimal totalCharge, Status status,
				   PaymentStatus payStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.bookingID = bookingID;
		this.guestID = guestID;
		this.propertyID = propertyID;
		this.arrivalDate = arrivalDate;
		this.departureDate = departureDate;
		this.baseCharge = baseCharge;
		this.gct = calculateGCT();
		this.tef = calculateTEF();
		this.gart = calculateGART();
		this.totalCharge = calculateTotalCharge();
		this.status = status;
		this.payStatus = payStatus;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	
	//COPY
	public Booking(Booking booking) {
		this.bookingID = booking.bookingID;
		this.guestID = booking.guestID;
		this.propertyID = booking.propertyID;
		this.arrivalDate = booking.arrivalDate;
		this.departureDate = booking.departureDate;
		this.baseCharge = booking.baseCharge;
		this.gct = booking.gct;
		this.tef = booking.tef;
		this.gart = booking.gart;
		this.totalCharge = booking.totalCharge;
		this.status = booking.status;
		this.payStatus = booking.payStatus;
		this.createdAt = booking.createdAt;
		this.updatedAt = booking.updatedAt;
	}


	
	//MUTATORS
	public void setBookingID(String bookingID) {
		this.bookingID = bookingID;
	}

	public void setGuestID(String guestID) {
		this.guestID = guestID;
	}

	public void setPropertyID(String propertyID) {
		this.propertyID = propertyID;
	}

	public void setArrivalDate(LocalDate arrivalDate) {
		this.arrivalDate = arrivalDate;
		recalculateCharges();
	}

	public void setDepartureDate(LocalDate departureDate) {
		this.departureDate = departureDate;
		recalculateCharges();
	}

	public void setBaseCharge(BigDecimal baseCharge){
		this.baseCharge = baseCharge;
		recalculateCharges();
	}

	public void setGct(BigDecimal gct) {
		this.gct = gct;
		recalculateCharges();
	}

	public void setTef(BigDecimal tef) {
		this.tef = tef;
		recalculateCharges();
	}

	public void setGart(BigDecimal gart) {
		this.gart = gart;
		recalculateCharges();
	}

	public void setTotalCharge(BigDecimal totalCharge) {
		this.totalCharge = totalCharge;
		recalculateCharges();
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setPayStatus(PaymentStatus payStatus) {
		this.payStatus = payStatus;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}



	//ACCESSORS
	public String getBookingID() {
		return bookingID;
	}

	public String getGuestID() {
		return guestID;
	}

	public String getPropertyID() {
		return propertyID;
	}

	public LocalDate getArrivalDate() {
		return arrivalDate;
	}

	public LocalDate getDepartureDate() {
		return departureDate;
	}

	public BigDecimal getBaseCharge(){
		return baseCharge;
	}

	public BigDecimal getGct() {
		return gct;
	}

	public BigDecimal getTef() {
		return tef;
	}

	public BigDecimal getGart() {
		return gart;
	}

	public BigDecimal getTotalCharge() {
		return totalCharge;
	}

	public Status getStatus() {
		return status;
	}

	public PaymentStatus getPayStatus() {
		return payStatus;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}


	//CALCULATE GCT
	public BigDecimal calculateGCT(){
		BigDecimal gctTotal = this.baseCharge.multiply(BigDecimal.valueOf(gctRate));
		return gctTotal;
	}

	//CALCULATE TEF
	public BigDecimal calculateTEF(){
		long numOfNights = ChronoUnit.DAYS.between(arrivalDate, departureDate);
		BigDecimal tefTotal = tefRate.multiply(BigDecimal.valueOf(numOfNights));
		return tefTotal;
	}

	//CALCULATE GART
	public BigDecimal calculateGART(){
		long numOfNights = ChronoUnit.DAYS.between(arrivalDate, departureDate);
		BigDecimal gartNightly = BigDecimal.ZERO;
		float avgRate = baseCharge.floatValue() /numOfNights;

		if (avgRate <= 20){
			gartNightly = BigDecimal.valueOf(1);
		}

		if (avgRate > 20 && avgRate <= 70){
			gartNightly = BigDecimal.valueOf(2);
		}

		if (avgRate > 70 && avgRate <= 110){
			gartNightly = BigDecimal.valueOf(3);
		}

		if (avgRate > 110){
			gartNightly = BigDecimal.valueOf(4);
		}

		BigDecimal gartTotal = gartNightly.multiply(BigDecimal.valueOf(numOfNights));
		return gartTotal;
	}


	//CALCULATE TOTAL CHARGE
	public BigDecimal calculateTotalCharge(){
		BigDecimal totalCharge = baseCharge.add(calculateGCT()).add(calculateGART().add(calculateTEF()));
		return totalCharge;
	}

	//RECALCULATE CHARGES
	public void recalculateCharges(){
		this.gct = calculateGCT();
		this.tef = calculateTEF();
		this.gart = calculateGART();
		this.totalCharge = calculateTotalCharge();
	}
	
	//TO STRING METHOD
	@Override
	public String toString() {

		String output = "\nBooking ID:     " + bookingID + "\n";
		output += "Guest ID:       " + guestID + "\n";
		output += "Property:       " + PropertyDAO.retrievePropertyByPropertyID(propertyID).getPropertyName() + " (" + propertyID + ")" + "\n";
		output += "Arrival Date:   " + arrivalDate + "\n";
		output += "Departure Date: " + departureDate + "\n";
		output += "Base Charge:    $" + String.format("%.2f", baseCharge) + "\n";
		output += "GCT:            $" + String.format("%.2f", calculateGCT()) + "\n";
		output += "TEF:            $" + String.format("%.2f", calculateTEF()) + "\n";
		output += "GART:           $" + String.format("%.2f", calculateGART()) + "\n";
		output += "Total Charge:   $" + String.format("%.2f", calculateTotalCharge()) + "\n";
		output += "Status:         " + status + "\n";
		output += "Payment Status: " + payStatus + "\n";
		output += "Created At:     " + createdAt + "\n";
		output += "Updated At:     " + updatedAt + "\n";
		
		return output;
	}
	
	
	//DISPLAY 
	public void displayBooking() {
		System.out.println(this);
	}
	

}
