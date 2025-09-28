package models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ServiceInvoice {
	private String invoiceID;
	private String customerID;
	private String serviceProviderID;
    private String description;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate payDate;
    private Status status;
    
    public enum Status{PAID, PENDING, CANCELLED}
    
    
    //CONSTRUCTORS
    
    //DEFAULT
    public ServiceInvoice() {
    	this.invoiceID = "";
    	this.customerID = "";
    	this.serviceProviderID = "";
    	this.description = "";
    	this.amount = new BigDecimal("0");
    	this.dueDate = null;
    	this.payDate = null;
    	this.status = Status.PENDING;
    }
    
    //PRIMARY
    public ServiceInvoice(String invoiceID, String customerID, String serviceProviderID,String description, BigDecimal amount,
    		 LocalDate dueDate, LocalDate payDate, Status status) {
    	this.invoiceID = invoiceID;
    	this.customerID = customerID;
    	this.serviceProviderID = serviceProviderID;
    	this.description = description;
    	this.amount = amount;
    	this.dueDate = dueDate;
    	this.payDate = payDate;
    	this.status = status;
    }
    
    //COPY
    public ServiceInvoice(ServiceInvoice invoice) {
    	this.invoiceID = invoice.invoiceID;
    	this.customerID = invoice.customerID;
    	this.serviceProviderID = invoice.serviceProviderID;
    	this.description = invoice.description;
    	this.amount = invoice.amount;
    	this.dueDate = invoice.dueDate;
    	this.payDate = invoice.payDate;
    	this.status = invoice.status;
    }

	//SETTERS & GETTERS
	public String getInvoiceID() {
		return invoiceID;
	}

	public void setInvoiceID(String invoiceID) {
		this.invoiceID = invoiceID;
	}

	public String getCustomerID() {
		return customerID;
	}

	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

	public String getServiceProviderID() {
		return serviceProviderID;
	}

	public void setServiceProviderID(String serviceProviderID) {
		this.serviceProviderID = serviceProviderID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public LocalDate getPayDate() {
		return payDate;
	}

	public void setPayDate(LocalDate payDate) {
		this.payDate = payDate;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}


	//TO STRING METHOD
	@Override
    public String toString() {
    	String output = "                 SERVICE INVOICE                         \n";
    	output += "===============================================================\n\n";
    	output+= "Invoice ID:" + invoiceID + "\n";
    	output+= "Customer ID:" + customerID + "\n";
    	output+= "Service Provider ID:" + serviceProviderID + "\n";
    	output+= "Description:" + description + "\n";
    	output+= "Charge: $" + amount + "\n";
    	output+= "Due Date: " + dueDate + "\n";
    	output+= "Pay Date:" + payDate + "\n";
    	output+= "Status:" + status + "\n";
    	return output;
    	
    }


	//DISPLAY SERVICE INVOICE
	public void displayServiceInvoice(){
		System.out.println(this);
	}
}
