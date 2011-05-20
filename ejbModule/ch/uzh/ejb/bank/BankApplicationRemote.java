package ch.uzh.ejb.bank;
import java.util.Date;
import java.util.List;

import javax.ejb.Remote;

import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;

@Remote
public interface BankApplicationRemote {
	public void remove();
	
	public Customer createCustomer(String userName, String password, String firstName, String lastName, String address, 
			Customer.Gender gender, String nationality);
	public List<Customer> getCustomer(String firstName, String lastName);
	public Customer getCustomer(long id);
	
	public Account createAccount(double balance, Account.Type accountType,
			float interest, double creditLimit, Customer customer);
	public Account getAccount(long accountNumber); 
	public List<Account> getAccounts(Customer customer);
	public void setAccountStatus(Account account, Account.Status status);
	
	public Account deposit(Account toAccount, double value);
	public Account withdraw(Account fromAccount, double value);
	public void transfer(Account fromAccount, Account toAccount, double value);
	
	public String getAccountHistory(Account account, Date from, Date to);
	
	public double getTotalBalance(Customer customer);
	public double getIncome(Customer customer, Date from, Date to);
	public double getNetChange(Customer customer, Date from, Date to);
	
	// stateful stuff
	public void selectAccount(long id);
	public long getSelectedAccountId();
	
	// session experiment
	public void test();
}
