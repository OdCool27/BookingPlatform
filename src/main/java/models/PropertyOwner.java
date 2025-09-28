package models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PropertyOwner extends User{
	private String ownerID;
    private BigDecimal balance; // Current earnings available for withdrawal
    private BigDecimal totalEarnings; // Lifetime earnings
    private PaymentDetails paymentDetails;
	private PayoutDetails payoutDetails;
	private List<Property> properties; //List of PropertyIDs
    private List<Transaction> transactions; // List of TransactionIDs
    private List<ServiceInvoice> outstandingInvoices; //List of InvoiceIDs
	
	//CONSTRUCTOR
	
	//DEFAULT
	public PropertyOwner() {
		super();
		this.ownerID = "";
		this.properties = new ArrayList<>();
		this.balance = new BigDecimal("0.0");
		this.totalEarnings = new BigDecimal("0.0");
		this.paymentDetails = new PaymentDetails();
		this.payoutDetails = new PayoutDetails();
		this.transactions = new ArrayList<>();
		this.outstandingInvoices = new ArrayList<>();
	}
	
	//PRIMARY
	public PropertyOwner(User user, String ownerID, BigDecimal balance, BigDecimal totalEarnings,
						 PaymentDetails paymentDetails, PayoutDetails payoutDetails, List<Property> properties,
						 List<Transaction> transactions, List<ServiceInvoice> outstandingInvoices) {
		super(user);
		this.ownerID = ownerID;
		this.properties = properties;
		this.balance = balance;
		this.totalEarnings = totalEarnings;
		this.paymentDetails = paymentDetails;
		this.payoutDetails = payoutDetails;
		this.transactions = transactions;
		this.outstandingInvoices = outstandingInvoices;
	}
	
	//COPY
	public PropertyOwner(PropertyOwner pO) {
		super(pO);
		this.ownerID = pO.ownerID;
		this.properties = pO.properties;
		this.balance = pO.balance;
		this.totalEarnings = pO.totalEarnings;
		this.paymentDetails = pO.paymentDetails;
		this.payoutDetails = pO.payoutDetails;
		this.transactions = pO.transactions;
		this.outstandingInvoices = pO.outstandingInvoices;
	}
	
	
	//ACCESSOR
	public String getOwnerID() {
		return ownerID;
	}
		
	public List<Property> getProperties() {
		return properties;
	}
	
	public BigDecimal getBalance() {
		return balance;
	}

	public BigDecimal getTotalEarnings() {
		return totalEarnings;
	}

	public PaymentDetails getChargeDetails() {
		return paymentDetails;
	}

	public PayoutDetails getPayoutDetails(){return payoutDetails;}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public List<ServiceInvoice> getOutstandingInvoices() {
		return outstandingInvoices;
	}

	//MUTATORS
	public void setOwnerID(String ownerID) {
		this.ownerID = ownerID;
	}
	
	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}
	
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public void setTotalEarnings(BigDecimal totalEarnings) {
		this.totalEarnings = totalEarnings;
	}

	public void setChargeDetails(PaymentDetails paymentDetails) {
		this.paymentDetails = paymentDetails;
	}

	public void setPayoutDetails(PayoutDetails payoutDetails){this.payoutDetails = payoutDetails;}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public void setOutstandingInvoices(List<ServiceInvoice> outstandingInvoices) {
		this.outstandingInvoices = outstandingInvoices;
	}

	//TO STRING METHOD
	@Override
	public String toString() {
		String output = super.toString();
		output += "Owner ID: " + ownerID + "\n";
		output += "Number of Properties: " + properties.size() + "\n";
		output += "Balance: $" +  String.format("%.2f", balance) + "\n";
		output += "Total Earnings: $" + String.format("%.2f", totalEarnings) + "\n";
		return output;
	}
	
	//DISPLAY METHOD
	public void displayPropertyOwner() {
		System.out.println(this);
	}
	
	
	
	
	
	
	
	
}
