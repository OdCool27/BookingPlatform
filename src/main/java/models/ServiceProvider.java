package models;

public class ServiceProvider extends User{
	private String serviceProviderID;
	private String companyName;
	private ServiceType type;
	
	
	//ENUM
	public enum ServiceType{
		HOUSEKEEPING, LANDSCAPING, TRANSPORTATION, MAINTENANCE, OTHER
	}
	
	
	//CONSTRUCTORS
	public ServiceProvider() {
		super();
		this.serviceProviderID = "";
		this.companyName = "";
		this.type = null;
	}
	
	public ServiceProvider(User u, String serviceProviderID, String companyName, ServiceType type) {
		super(u);
		this.serviceProviderID = serviceProviderID;
		this.companyName = companyName;
		this.type = type;
	}
	
	public ServiceProvider(ServiceProvider sP) {
		super(sP);
		this.serviceProviderID = sP.serviceProviderID;
		this.companyName = sP.companyName;
		this.type = sP.type;
	}
	

	//ACCESSORS
	public String getServiceProviderID() {
		return serviceProviderID;
	}
	
	public String getCompanyName() {
		return companyName;
	}

	public ServiceType getType() {
		return type;
	}
	

	//MUTATORS
	public void setServiceProviderID(String serviceProviderID) {
		this.serviceProviderID = serviceProviderID;
	}
	
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public void setType(ServiceType type) {
		this.type = type;
	}
	
	
	//TO STRING METHOD
	@Override
	public String toString() {
		String output = super.toString()
				+ "ServiceProviderID: " + serviceProviderID + "\n"
				+ "Company Name: " + companyName + "\n"
				+ "Service Type: " + type;
		return output;
	}
	
	
	//DISPLAY METHOD
	public void displayServiceProvider() {
		System.out.println(toString());
	}
	
	
	
	

}
