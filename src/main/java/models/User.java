package models;

import helpers.PasswordSecurity;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {
	protected int userID;
	protected String email;
	protected String passwordHash;
	protected String firstName;
	protected String lastName;
	protected Gender gender;
	protected String contactNum;
	protected String profilePictureURL;
	protected boolean isActivated;
	protected String activationCode;//no display required
	protected LocalDateTime activationCodeExpiry;//no display required


	//GENDER ENUM
	public enum Gender{
		MALE, FEMALE, NOT_SPECIFIED
	}


	//CONSTRUCTORS
	//DEFAULT
	public User() {
		this.userID = 0;
		this.email ="";
		this.passwordHash = "";
		this.firstName = "";
		this.lastName = "";
		this.gender = Gender.NOT_SPECIFIED;
		this.contactNum = "";
		this.profilePictureURL = "https://profilepic.com";
		this.isActivated = false;
		this.activationCode = null;
		this.activationCodeExpiry = null;
	}
	
	//PRIMARY
	public User(int userID, String email, String passwordHash, String firstName, String lastName, Gender gender,
		String contactNum, String profilePictureURl, boolean isActivated, String activationCode,
		LocalDateTime activationCodeExpiry) {
		this.userID = userID;
		this.email = email;
		this.passwordHash = passwordHash;
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
		this.contactNum = contactNum;
		this.profilePictureURL = profilePictureURl;
		this.isActivated = isActivated;
		this.activationCode = activationCode;
		this.activationCodeExpiry = activationCodeExpiry;
	}
	
	//COPY
	public User(User user) {
		this.userID = user.userID;
		this.email = user.email;
		this.passwordHash = user.passwordHash;
		this.firstName = user.firstName;
		this.lastName = user.lastName;
		this.gender = user.gender;
		this.contactNum = user.contactNum;
		this.profilePictureURL = user.profilePictureURL;
		this.isActivated = user.isActivated;
		this.activationCode = user.activationCode;
		this.activationCodeExpiry = user.activationCodeExpiry;
	}

	
	//MUTATORS
	public void setUserID(int userID){
		this.userID = userID;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setGender(Gender gender){
		this.gender =gender;
	}

	public void setContactNum(String contactNum) {
		this.contactNum = contactNum;
	}
	
	public void setProfilePictureURL(String profilePictureURL) {
		this.profilePictureURL = profilePictureURL;
	}	

	public void setActivated(boolean isActivated) {
		this.isActivated = isActivated;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	public void setActivationCodeExpiry(LocalDateTime activationCodeExpiry) {
		this.activationCodeExpiry = activationCodeExpiry;
	}
	
	

	//ACCESSORS
	public int getUserID(){
		return userID;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public Gender getGender(){
		return gender;
	}

	public String getContactNum() {
		return contactNum;
	}
	
	public String getProfilePictureURL() {
		return profilePictureURL;
	}
	
	public boolean isActivated() {
		return isActivated;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public LocalDateTime getActivationCodeExpiry() {
		return activationCodeExpiry;
	}
	

	//TO STRING METHOD
	@Override
	public String toString() {
		String output = profilePictureURL;
		output += "\nUserID: " + userID +"\n";
		output += "Name: " + firstName + " " + lastName +"\n";
		output += "Gender: " + gender.toString() + "\n";
		output += "Email: " + email + "\n";
		output += "Password Hash: " + PasswordSecurity.maskPassword(passwordHash) + "\n";
		output += "Contact Num: " + contactNum + "\n";
		output += "Activation: " + (isActivated ? "Yes":"No") + "\n";
		output += "Activation Code: " + activationCode + "\n";
		output += "Activation Code Expiration: " + activationCodeExpiry + "\n";
		return output;
	}


	//EQUALS METHOD
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return userID == user.userID && isActivated == user.isActivated && Objects.equals(email, user.email) && Objects.equals(passwordHash, user.passwordHash) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName) && gender == user.gender && Objects.equals(contactNum, user.contactNum) && Objects.equals(profilePictureURL, user.profilePictureURL) && Objects.equals(activationCode, user.activationCode) && Objects.equals(activationCodeExpiry, user.activationCodeExpiry);
	}

	//HASH METHOD
	@Override
	public int hashCode() {
		return Objects.hash(userID, email, passwordHash, firstName, lastName, gender, contactNum, profilePictureURL, isActivated, activationCode, activationCodeExpiry);
	}

	//DISPLAY METHOD
	public void displayUser() {
		System.out.println(toString());
	}
	
	

}
