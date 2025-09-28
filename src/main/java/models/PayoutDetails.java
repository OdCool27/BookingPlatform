package models;

import java.util.Currency;

public class PayoutDetails {
    private String payoutAccountID; //Unique Identifier returned by the API
    private String bankLast4Digits;
    private Currency currency; //e.g., USD, JMD, CAD;
    private String country;
    private boolean payoutEnabled;

    //CONSTRUCTORS
    public PayoutDetails() {
        this.payoutAccountID = "";
        this.bankLast4Digits = "";
        this.currency = Currency.getInstance("USD");
        this.country = "";
        this.payoutEnabled = false;
    }

    public PayoutDetails(String payoutAccountID, String bankLast4Digits, Currency currency, String country,
                         boolean payoutEnabled){
        this.payoutAccountID = payoutAccountID;
        this.bankLast4Digits = bankLast4Digits;
        this.currency = currency;
        this.country = country;
        this.payoutEnabled = payoutEnabled;
    }

    public PayoutDetails(PayoutDetails pD){
        this.payoutAccountID = pD.payoutAccountID;
        this.bankLast4Digits = pD.bankLast4Digits;
        this.currency = pD.currency;
        this.country = pD.country;
        this.payoutEnabled = pD.payoutEnabled;
    }

    //ACCESSORS
    public String getPayoutAccountID() {
        return payoutAccountID;
    }

    public String getBankLast4Digits() {
        return bankLast4Digits;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getCountry() {
        return country;
    }

    public boolean isPayoutEnabled(){
        return payoutEnabled;
    }


    //MUTATORS
    public void setPayoutAccountID(String payoutAccountID) {
        this.payoutAccountID = payoutAccountID;
    }

    public void setBankLast4Digits(String bankLast4Digits) {
        this.bankLast4Digits = bankLast4Digits;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setPayoutEnabled(boolean payoutEnabled){
        this.payoutEnabled = payoutEnabled;
    }


    //TO STRING METHOD
    public String toString(){
        String output  = "id: " + payoutAccountID;
        output += "\nbank acc: *********" + bankLast4Digits;
        output += "\ncurrency: " + currency;
        output += "\ncountry: " + country;
        output += "\nPayment enabled: " + (payoutEnabled?"Yes":"No");
        return output;
    }

    //DISPLAY METHOD
    public void displayPayoutDetails(){
        System.out.println(toString());
    }
}