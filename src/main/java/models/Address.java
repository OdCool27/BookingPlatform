package models;

public class Address {
	private String street;
	private String suite;
	private String city;
	private String state;
	private String postalCode;
	private String country;
	
	
	//CONSTRUCTORS
	
	//DEFAULT
	public Address() {
		this.street = "";
	    this.suite = "";
	    this.city = "";
	    this.state = "";
	    this.postalCode = "";
	    this.country = "";
	}
	
	//PRIMARY
	public Address(String street, String suite, String city, String state,
			String postalCode, String country) {
		this.street = street;
		this.suite = suite;
		this.city = city;
		this.state = state;
		this.postalCode = postalCode;
		this.country = country;
	}
	
	//COPY
	public Address(Address addr) {
		this.street = addr.street;
		this.suite = addr.suite;
		this.city = addr.city;
		this.state = addr.state;
		this.postalCode = addr.postalCode;
		this.country = addr.country;
	}
	
	//MUTATORS
	public void setStreet(String street) {
		this.street = street;
	}

	public void setSuite(String suite) {
		this.suite = suite;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public void setCountry(String country) {
		this.country = country;
	}


	
	//ACCESSORS
	public String getStreet() {
		return street;
	}

	public String getSuite() {
		return suite;
	}

	public String getState() {
		return state;
	}

	public String getCity() {
		return city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getCountry() {
		return country;
	}
	
	
	
	//TO-STRING METHOD
	@Override
	public String toString() {
		String output = (!street.isBlank() ? street+ ", ":"");
		output += (!suite.isBlank() ? suite + ", ":"");
		output += (!city.isBlank() ? city + ", ":"");
		output += (!state.isBlank() ? state + ", ":"");
		output += (!postalCode.isBlank() ? postalCode + ", ":"");
		output += (!country.isBlank() ? country :"");
		return output;
	}
	
	
	//DISPLAY METHOD
	public void displayAddress() {
		System.out.println(this);
	}

}
