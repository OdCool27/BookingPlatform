package models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class Transaction {
    private String transactionID;
    private String bookingID;     // Can be null for things like manual payouts
    private BigDecimal amount;
    private LocalDateTime timeStamp;
    private TransactionType type;

    public enum TransactionType {
        EARNING,
        WITHDRAWAL,
        ADJUSTMENT
    }

    public Transaction(){
        this.transactionID = "";
        this.bookingID = "";
        this.amount = new BigDecimal("0");
        this.timeStamp = LocalDateTime.now();
        this.type = null;
    }

    public Transaction(String transactionID, String bookingID, BigDecimal amount, LocalDateTime timeStamp,
                       TransactionType type) {
        this.transactionID = transactionID;
        this.bookingID = bookingID;
        this.amount = amount;
        this.timeStamp = timeStamp;
        this.type = type;
    }

    // Getters and setters
    //GETTERS
    public String getTransactionID() {
        return transactionID;
    }

    public String getBookingID() {
        return bookingID;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public TransactionType getType() {
        return type;
    }


    //SETTERS
    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }


    //TO STRING METHOD
    public String toString(){
        String output = "Transaction ID" + transactionID;
        output += "Booking ID: " + bookingID;
        output += "Amount: $" + amount;
        output += "Date: " + timeStamp;
        output += "Transaction Type: " + type;
        return output;
    }

    //DISPLAY TRANSACTION
    public void displayTransaction(){
        System.out.println(this);
    }
}

