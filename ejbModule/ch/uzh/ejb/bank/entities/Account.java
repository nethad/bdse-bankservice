package ch.uzh.ejb.bank.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name="ACCOUNTS")
@NamedQueries({
	@NamedQuery(name="Account.findById",
		query="SELECT DISTINCT OBJECT(a) FROM Account a WHERE a.accountId=:id"),
	@NamedQuery(name="Account.findByCustomer",
		query="SELECT OBJECT(a) FROM Account a WHERE a.customer=:customer"),
	@NamedQuery(name="Account.findAllAccounts",
				query="SELECT OBJECT(a) FROM Account a")
})
public class Account implements Serializable {

	private static final long serialVersionUID = 1029374316591233692L;
	
	public enum Status {
		OPEN,
		CLOSED
	}
	
	public enum Type {
		PRIVATE_DEBIT,
		PRIVAE_CREDIT,
		PRIVATE_EQUITIES
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ACCOUNTS_SEQ")
	@SequenceGenerator(name="ACCOUNTS_SEQ", sequenceName="ACCOUNTS_SEQ")
	private long accountId;
	private double balance;
	private Type accountType;
	private float interest;
	private double creditLimit;
	
	@Enumerated(EnumType.STRING)
	private Status accountStatus;

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="customerId")
	private Customer customer;
	
	public Account() {}

	public Account(double balance, Type accountType,
			float interest, double creditLimit) {
		this.balance = balance;
		this.accountType = accountType;
		this.interest = interest;
		this.creditLimit = creditLimit;
		this.accountStatus = Status.CLOSED;
	}

	public long getAccountId() {
		return accountId;
	}

	public void setAccountId(long accountID) {
		this.accountId = accountID;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public Type getAccountType() {
		return accountType;
	}

	public void setAccountType(Type accountType) {
		this.accountType = accountType;
	}

	public float getInterest() {
		return interest;
	}

	public void setInterest(float interest) {
		this.interest = interest;
	}

	public double getCreditLimit() {
		return creditLimit;
	}

	public void setCreditLimit(double creditLimit) {
		this.creditLimit = creditLimit;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		if(this.customer instanceof Customer) {
			this.customer.getAccounts().remove(this);
		}
		this.customer = customer;
		if(!customer.getAccounts().contains(this)) {
			customer.getAccounts().add(this);
		}
	}
	
	public Status getAccountStatus() {
		return accountStatus;
	}
	
	public void setAccountStatus(Status accountStatus) {
		this.accountStatus = accountStatus;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		
		if(obj instanceof Account) {
			Account otherAccount = ((Account) obj);
			return (accountId == otherAccount.accountId);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return String.valueOf(accountId).hashCode();
	}
}
