package models;

import java.time.LocalDateTime;

public class Review {
	private int reviewID;
	private String guestID;
	private String propertyID;
	private String bookingID;
	private int rating;
	private String comment;
	private LocalDateTime timeStamp;
	
	//CONSTRUCTOR
	
	//DEFAULT
	public Review() {
		this.reviewID = 0;
		this.guestID = "";
		this.propertyID = "";
		this.bookingID = "";
		this.rating = 0;
		this.comment = "";
		timeStamp = LocalDateTime.now();
	}
	
	//PRIMARY
	public Review(int reviewID, String guestID, String propertyID, String bookingID,
				  int rating, String comment, LocalDateTime timeStamp) {
		this.reviewID = reviewID;
		this.guestID = guestID;
		this.propertyID = propertyID;
		this.bookingID = bookingID;
		this.rating = rating;
		this.comment = comment;
		this.timeStamp = timeStamp;
	}
	
	//COPY
	public Review(Review r) {
		this.reviewID = r.reviewID;
		this.guestID = r.guestID;
		this.propertyID = r.propertyID;
		this.bookingID = r.bookingID;
		this.rating = r.rating;
		this.comment = r.comment;
		this.timeStamp = r.timeStamp;
	}

	
	//ACCESSORS
	public int getReviewID() {
		return reviewID;
	}

	public String getGuestID() {
		return guestID;
	}

	public String getPropertyID() {
		return propertyID;
	}

	public String getBookingID() {
		return bookingID;
	}

	public int getRating() {
		return rating;
	}

	public String getComment() {
		return comment;
	}

	public LocalDateTime getTimeStamp() {
		return timeStamp;
	}


	//MUTATORS
	public void setReviewID(int reviewID) {
		this.reviewID = reviewID;
	}

	public void setGuestID(String guestID) {
		this.guestID = guestID;
	}

	public void setPropertyID(String propertyID) {
		this.propertyID = propertyID;
	}

	public void setBookingID(String bookingID) {
		this.bookingID = bookingID;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setTimeStamp(LocalDateTime timeStamp) {
		this.timeStamp = timeStamp;
	}

	//TO STRING METHOD
	@Override
	public String toString() {
		String output = "ReviewID: " + reviewID + "\n";
		output += "GuestID: " + guestID + "\n";
		output += "PropertyID: " + propertyID + "\n";
		output += "BookingID: " + bookingID + "\n";
		output += "Rating: " + rating + "/5\n";
		output += "Comment: " + comment + "\n";
		output += "TimeStamp: " + timeStamp;
		return output;
	}
	
	
	//DISPLAY METHOD
	public void displayReview() {
		System.out.println(this);
	}
	

}
