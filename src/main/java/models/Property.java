package models;

import com.fasterxml.jackson.databind.annotation.JsonAppend;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Property {
	private String propertyID;
	private String ownerID;
	private String propertyName;
	private String description;
	private Address address;
	private double latitude;
	private double longitude;
	private double houseSize;//square feet
	private double landSize;//square feet
	private BigDecimal baseRate;
	private List<String> photosURL;
	private List<String> facilities;
	private List<Review> reviews;
	private List<PricingEntry> priceList;
	private PropertyVerification verificationStatus;


	
	
	//CONSTRUCTORS
	
	//DEFAULT
	public Property() {
	    this.propertyID = "";
	    this.ownerID = "";
	    this.propertyName = "";
	    this.description = "description here";
	    this.address = new Address();
		this.latitude = 0;
		this.longitude = 0;
	    this.houseSize = 0;
	    this.landSize = 0;
	    this.photosURL = new ArrayList<>();
	    this.facilities = new ArrayList<>();
	    this.reviews = new ArrayList<>();
	    this.baseRate = new BigDecimal("0");
	    this.priceList = new ArrayList<>();
		this.verificationStatus = new PropertyVerification();
	}
	
	
	//PRIMARY
	public Property(String propertyID, String ownerID, String propertyName, String description, Address address,
					double latitude, double longitude, double houseSize, double landSize, List<String> photosURL,
					List<String> facilities, List<Review> reviews, BigDecimal baseRate, List<PricingEntry> priceList,
					PropertyVerification verificationStatus) {
		this.propertyID = propertyID;
		this.ownerID = ownerID;
		this.propertyName = propertyName;
		this.description = description;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
		this.houseSize = houseSize;
		this.landSize = landSize;
		this.photosURL = photosURL;
		this.facilities = facilities;
		this.reviews = reviews;
		this.baseRate = baseRate;
		this.priceList = priceList;
		this.verificationStatus =verificationStatus;
	}
	
	
	//COPY
	public Property(Property other) {
	    this.propertyID = other.propertyID;
	    this.ownerID = other.ownerID;
	    this.propertyName = other.propertyName;
	    this.description = other.description;
	    this.address = other.address;
		this.latitude = other.latitude;
		this.longitude = other.longitude;
		this.houseSize = other.houseSize;
		this.landSize = other.landSize;
		this.photosURL = other.photosURL;
		this.facilities = other.facilities;
	    this.reviews = other.reviews;
	    this.baseRate = other.baseRate;
	    this.priceList = other.priceList;
		this.verificationStatus = other.verificationStatus;
	}



	//MUTATORS
	
	public void setPropertyID(String propertyID) {
		this.propertyID = propertyID;
	}

	public void setOwnerID(String ownerID) {
		this.ownerID = ownerID;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public void setLatitude(double latitude){
		this.latitude = latitude;
	}

	public void setLongitude(double longitude){
		this.longitude = longitude;
	}
	
	public void setHouseSize(double houseSize) {
		this.houseSize = houseSize;
	}
	
	public void setLandSize(double landSize) {
		this.landSize = landSize;
	}
	
	public void setPhotosURL(List<String> photosURL) {
		this.photosURL = photosURL;
	}

	public void setFacilities(List<String> facilities) {
		this.facilities = facilities;
	}

	public void setReview(List<Review> reviews) {
		this.reviews = reviews;
	}
	
	public void setBaseRate(BigDecimal baseRate) {
		this.baseRate = baseRate;
	}
	
	public void setPriceList(List<PricingEntry> priceList) {
		this.priceList = priceList;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

	public void setVerificationStatus(PropertyVerification verificationStatus) {
		this.verificationStatus = verificationStatus;
	}

	//ACCESSORS
	public String getPropertyID() {
		return propertyID;
	}
	
	public String getOwnerID() {
		return ownerID;
	}

	public String getPropertyName() {
		return propertyName;
	}
	
	public String getDescription() {
		return description;
	}

	public Address getAddress() {
		return address;
	}

	public double getLatitude(){
		return latitude;
	}

	public double getLongitude(){
		return longitude;
	}
	
	public double getHouseSize() {
		return houseSize;
	}
	
	public double getLandSize() {
		return landSize;
	}
	
	public List<String> getPhotosURL() {
		return photosURL;
	}

	public List<String> getFacilities() {
		return facilities;
	}

	public List<Review> getReviews() {
		return reviews;
	}
	
	public BigDecimal getBaseRate() {
		return baseRate;
	}
	
	public List<PricingEntry> getPriceList(){
		return priceList;
	}

	public PropertyVerification getVerificationStatus() {
		return verificationStatus;
	}

	//TO-STRING METHOD
	@Override
	public String toString() {
		String output = "Property ID: " + propertyID + "\n";
		output += "Owner ID: " + ownerID + "\n";
		output += "Property Name: " + propertyName + "\n";
		output += "Description: " + description + "\n";
		output += "Address: " + address + "\n";
		output += "Latitude: " + latitude + "\n";
		output += "Longitude: " + longitude + "\n";
		output += "House Size: " + houseSize + "sqr ft\n";
		output += "Land Size: " + landSize + "sqr ft\n";
		output += "Base Nightly Rate: $" + String.format("%.2f", baseRate)  + "\n";
		output += "Facilities: ";
		
		//Outputs List of Facilities
		for (String f: facilities) {
			output += f + ", ";
		}

		//Outputs List of Photos
		for (String p: photosURL) {
			output += p + "\n ";
		}
		
		output += "\nAverage Rating :";
		
		double avgRating = calculateAverageRating();
		
		if (!reviews.isEmpty()) {
			output += String.format("%.1f", avgRating);
		}else {
			output += "N/A";
		}
		
		return output;
	}


	//CALCULATE AVERAGE RATINGS
	public double calculateAverageRating(){
		double rating = 0;
		for(Review r: reviews){
			rating += r.getRating();
		}
		return (rating/reviews.size());
	}
	
	
	//DISPLAY METHOD
	public void displayProperty() {
		System.out.println(this);
	}

	//EQUALS METHOD
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Property property = (Property) o;
		return Objects.equals(propertyID, property.propertyID);
	}


	//HASH CODE METHOD
	@Override
	public int hashCode() {
		return Objects.hash(propertyID, ownerID, propertyName, description, address, latitude, longitude, houseSize, landSize, photosURL, facilities, reviews, baseRate, priceList);
	}
}
