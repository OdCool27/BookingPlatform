package models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PricingEntry {
	private String propertyID;
	private LocalDate startDate;
	private LocalDate endDate;
	private BigDecimal nightlyRate;
	
	
	//CONSTRUCTORS
	//DEFAULT
	public PricingEntry() {
		this.propertyID = "";
		this.startDate = LocalDate.now();
		this.endDate = LocalDate.now().plusDays(10);
		this.nightlyRate = new BigDecimal("0");
	}
	
	//PRIMARY
	public PricingEntry(String propertyID, LocalDate startDate, LocalDate endDate, BigDecimal nightlyRate) {
		this.propertyID = propertyID;
		this.startDate = startDate;
		this.endDate = endDate;
		this.nightlyRate = nightlyRate;
	}
	
	//COPY
	public PricingEntry(PricingEntry pE) {
		this.propertyID = pE.propertyID;
		this.startDate = pE.startDate;
		this.endDate = pE.endDate;
		this.nightlyRate = pE.nightlyRate;
	}

	
	
	//MUTATORS
	public void setPropertyID(String propertyID){
		this.propertyID = propertyID;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public void setNightlyRate(BigDecimal nightlyRate) {
		this.nightlyRate = nightlyRate;
	}
	
	
	//ACCESSORS
	public String getPropertyID(){
		return propertyID;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public BigDecimal getNightlyRate() {
		return nightlyRate;
	}
	
	
	//TO STRING METHOD
	@Override
	public String toString() {
		String output = String.format("Pricing: $%.2f", nightlyRate) + "\n";
		output += "Date period: " + startDate + " - " + endDate;
		return output;
	}
	
	
	//DISPLAY METHOD
	public void displayPricingEntry() {
		System.out.println(this);
	}
	

}
