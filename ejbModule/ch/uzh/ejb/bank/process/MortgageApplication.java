package ch.uzh.ejb.bank.process;

import ch.uzh.ejb.bank.entities.Customer;

public class MortgageApplication {

	private boolean notClosed = true;
	private double requiredSum;
	private double availableFunds;
	private boolean approved = false;
	private boolean signed = false;
	private Customer customer;
	
	public MortgageApplication(Customer customer) {
		this.customer = customer;
	}
	
	public void setAvailableFunds(double availableFunds) {
		this.availableFunds = availableFunds;
	}
	
	public double getAvailableFunds() {
		return availableFunds;
	}
	
	public double getRequiredSum() {
		return requiredSum;
	}
	
	public void setRequiredSum(double sum) {
		this.requiredSum = sum;
	}
	
	public Customer getCustomer() {
		return customer;
	}
	
	public void setApproved(boolean approved) {
		this.approved = approved;
	}
	
	public boolean isApproved() {
		return approved;
	}
	
	public void setSigned(boolean signed) {
		this.signed = signed;
	}
	
	public boolean isSigned() {
		return signed;
	}
	
	public void setClosed() {
		this.notClosed = false;
	}
	
	public boolean isClosed() {
		return !notClosed;
	}

}
