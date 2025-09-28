package models;

import java.util.Objects;

public class PaymentDetails {
	protected String paymentMethodID;
	protected String paymentUserID;
	protected PaymentType paymentType;      // STRIPE, PAYPAL, etc.
	protected boolean isDefault;
	protected String nickname;       // Optional display name

	public enum PaymentType{STRIPE, PAYPAL, NONE}
	
	public PaymentDetails() {
		this.paymentMethodID = "";
		this.paymentUserID = "";
		this.paymentType = PaymentType.NONE;
		this.isDefault = false;
		this.nickname = "";
	}
	
	public PaymentDetails(String paymentMethodID, String paymentUserID, PaymentType paymentType, boolean isDefault,
						  String nickname) {
		this.paymentMethodID = paymentMethodID;
		this.paymentUserID = paymentUserID;
		this.paymentType = paymentType;
		this.isDefault = isDefault;
		this.nickname = nickname;
	}
	
	public PaymentDetails(PaymentDetails p) {
		this.paymentMethodID = p.paymentMethodID;
		this.paymentUserID = p.paymentUserID;
		this.paymentType = p.paymentType;
		this.isDefault = p.isDefault;
		this.nickname = p.nickname;
	}

	
	//ACCESSORS
	public String getPaymentMethodID() {
		return paymentMethodID;
	}

	public String getPaymentUserID() {
		return paymentUserID;
	}

	public PaymentType getPaymentType() {
		return paymentType;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public String getNickname() {
		return nickname;
	}

	//MUTATORS
	public void setPaymentMethodID(String paymentID) {
		this.paymentMethodID = paymentID;
	}

	public void setPaymentUserID(String userID) {
		this.paymentUserID = userID;
	}

	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}

	public void setDefault(boolean aDefault) {
		isDefault = aDefault;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}



	//TO STRING METHOD
	public String toString(){
		String output = "Payment Method ID:" + paymentMethodID;
		output  += "\nPayment User ID: " + paymentUserID;
		output  += "\nPayment Type: " + paymentType;
		output  += "\nDefault: " + isDefault;
		output  += "\nNickname: " + nickname;
		return output;
	}


	//DISPLAY METHOD
	public void displayChargeDetails(){
		System.out.println(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		PaymentDetails that = (PaymentDetails) o;
		return isDefault == that.isDefault && Objects.equals(paymentMethodID, that.paymentMethodID) &&
				Objects.equals(paymentUserID, that.paymentUserID) && paymentType == that.paymentType && Objects.equals(nickname, that.nickname);
	}

	@Override
	public int hashCode() {
		return Objects.hash(paymentMethodID, paymentUserID, paymentType, isDefault, nickname);
	}
}
