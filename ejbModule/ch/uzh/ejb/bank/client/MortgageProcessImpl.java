package ch.uzh.ejb.bank.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Set;

import ch.uzh.ejb.bank.BankApplicationRemote;
import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;
import ch.uzh.ejb.bank.process.MortgageApplication;
import ch.uzh.ejb.bank.process.MortgageProcess;

public class MortgageProcessImpl extends MortgageProcess {

	BankApplicationRemote bankApplication;
	long customerId;
	long accountId;
	
	private enum Incomplete {
		CUSTOMER_NULL {
			@Override
			public String toString() {
				return "invalid customer";
			}
		},
		ACCOUNT_NULL {
			@Override
			public String toString() {
				return "invalid account";
			}
		},
		FIRSTNAME {
			@Override
			public String toString() {
				return "invalid first name";
			}
		},
		LASTNAME {
			@Override
			public String toString() {
				return "invalid last name";
			}
		},
		ADDRESS {
			@Override
			public String toString() {
				return "invalid address";
			}
		},
		NATIONALITY {
			@Override
			public String toString() {
				return "invalid nationality";
			}
		},
		GENDER {
			@Override
			public String toString() {
				return "invalid gender";
			}
		},
		ACCOUNT_CUSTOMER_MISMATCH {
			@Override
			public String toString() {
				return "account and user do not match";
			}
		}
	}
	Set<Incomplete> incomplete = null;
	
	public MortgageProcessImpl(BankApplicationRemote bankApplication, long customerId, long accountId) {
		this.bankApplication = bankApplication;
		this.customerId = customerId;
		this.accountId = accountId;
	}
	
	@Override
	protected void collectCustomerInformation() {
		this.customer = bankApplication.getCustomer(customerId);
		this.application = new MortgageApplication(this.customer);
	}

	@Override
	protected void contactCustomer() {
		if(!incomplete.isEmpty()) {
			System.out.println("Customer info not complete, please contact customer to fill in the misisng information.");
			if(incomplete.contains(Incomplete.ACCOUNT_NULL)) {
				System.out.print(Incomplete.ACCOUNT_NULL + " - Account ID: ");
				accountId = Long.parseLong(readString());
			}
			if(incomplete.contains(Incomplete.CUSTOMER_NULL)) {
				System.out.print(Incomplete.CUSTOMER_NULL + " - Customer ID: ");
				customerId = Long.parseLong(readString());
			}
			if(incomplete.contains(Incomplete.FIRSTNAME)) {
				System.out.print(Incomplete.FIRSTNAME + " - Customer First Name: ");
				customer.setFirstName(readString());
			}
			if(incomplete.contains(Incomplete.LASTNAME)) {
				System.out.print(Incomplete.LASTNAME + " - Customer Last Name: ");
				customer.setFirstName(readString());
			}
			if(incomplete.contains(Incomplete.ADDRESS)) {
				System.out.print(Incomplete.ADDRESS + " - Customer Address: ");
				customer.setFirstName(readString());
			}
			if(incomplete.contains(Incomplete.NATIONALITY)) {
				System.out.print(Incomplete.NATIONALITY + " - Customer Nationality: ");
				customer.setFirstName(readString());
			}
			if(incomplete.contains(Incomplete.GENDER)) {
				System.out.print(Incomplete.GENDER + " - Customer Gender: (");
				int ctr = 0;
				EnumSet<Customer.Gender> enums = EnumSet.allOf(Customer.Gender.class);
				for(Customer.Gender gender : enums) {
					System.out.print(ctr++ + " - " + gender);
				}
				System.out.print(") ");
				int gender = Integer.parseInt(readString());
				customer.setGender((Customer.Gender) enums.toArray()[gender]);
			}
			if(incomplete.contains(Incomplete.ACCOUNT_CUSTOMER_MISMATCH)) {
				System.out.print(Incomplete.ACCOUNT_CUSTOMER_MISMATCH + " - Customer ID: (");
				customerId = Long.parseLong(readString());
				System.out.print("Account ID: (");
				accountId = Long.parseLong(readString());
			}
		}
	}
	
	private String readString() {
		String str = null;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(getInputStream()));
		try {
			str = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return str;
	}

	protected InputStream getInputStream() {
		return System.in;
	}

	@Override
	protected void computeRequiredFunds() {
		//do whatever to compute the value here, here is just a very basic implementations etting a fixed value
		
		this.application.setRequiredSum(120000.0);
	}

	@Override
	protected void computeAvailableFunds() {
		double availableFunds = 0.0;
		try {
			availableFunds = bankApplication.getTotalBalance(this.customer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.application.setAvailableFunds(availableFunds);
	}

	@Override
	protected void aproovalProcess() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		
		double income = 0.0;
		try {
			income = bankApplication.getIncome(customer, cal.getTime(), new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//if last years income is at least 10% of the required sum, approove
		if(income >= application.getRequiredSum() * 0.1) {
			application.setApproved(true);
		}
	}

	@Override
	protected void sendDocuments() {
		System.out.println("Sending documents to: " + 
				customer.getFirstName() + " " + 
				customer.getLastName() + " " + 
				customer.getAddress());
		System.out.println("Did customer sign the documents? (y/n): ");
		String answer = readString();
		if(answer.trim().toLowerCase().equals("y")) {
			application.setSigned(true);
		} 
	}

	@Override
	protected void sendRejectionLetter() {
		System.out.println("Sending rejection letter to: " + 
				customer.getFirstName() + " " + 
				customer.getLastName() + " " + 
				customer.getAddress());
	}

	@Override
	protected void payOut() {
		try {
			if(application.getRequiredSum() > application.getAvailableFunds()) {
				bankApplication.selectAccount(accountId);
				bankApplication.payOutMortgage(application);
				System.out.println("Mortgage payed to account nr. " + accountId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void contactCustomerIfNotSigned() {
		System.out.println("Informing customer of cancellation of mortgage application.");
	}

	@Override
	protected void closeApplication() {
		application.setClosed();
		System.out.println("Application closed");
	}

	@Override
	protected boolean isCustomerComplete() {
		boolean complete = true;
		
		Account account = bankApplication.getAccount(accountId);
		if(account == null) {
			incomplete = EnumSet.of(Incomplete.ACCOUNT_NULL);
			return false;
		} 
		if(customer == null) {
			incomplete = EnumSet.of(Incomplete.CUSTOMER_NULL);
			return false;
		}
		Collection<Incomplete> incompletes = new LinkedList<Incomplete>();
		if(safe(customer.getFirstName()).isEmpty()) {
			incompletes.add(Incomplete.FIRSTNAME);
			complete = false;
		}
		if(safe(customer.getLastName()).isEmpty()) {
			incompletes.add(Incomplete.LASTNAME);
			complete = false;
		}
		if(safe(customer.getAddress()).isEmpty()) {
			incompletes.add(Incomplete.ADDRESS);
			complete = false;
		}
		if(safe(customer.getNationality()).isEmpty()) {
			incompletes.add(Incomplete.NATIONALITY);
			complete = false;
		}
		if(customer.getGender() == null || !(
			(	customer.getGender().equals(Customer.Gender.MALE) || 
				customer.getGender().equals(Customer.Gender.FEMALE) || 
				customer.getGender().equals(Customer.Gender.OTHER))
			)) {
			incompletes.add(Incomplete.GENDER);
			complete = false;
		}
		if(!account.getCustomer().equals(customer)) {
			incompletes.add(Incomplete.ACCOUNT_CUSTOMER_MISMATCH);
			complete = false;
		}

		if(!incompletes.isEmpty()) {
			incomplete = EnumSet.copyOf(incompletes);
		}
		return complete; 
	}
	
	/**
	 * Returns a string that is safe to use.
	 * 
	 * @param str	the string.
	 * @return 
	 * 		{@code str} with whitespace trimmed at the beginning and end. 
	 * 		If {@code str} is null this method returns an empty string.
	 */
	private String safe(String str) {
		if(str == null) {
			return "";
		} else {
			return str.trim();
		}
	}
}
