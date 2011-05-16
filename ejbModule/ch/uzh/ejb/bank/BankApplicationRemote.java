package ch.uzh.ejb.bank;
import java.util.List;

import javax.ejb.Remote;

@Remote
public interface BankApplicationRemote {
	public void remove();
	
	public Role createUserRole(String userName, String role);
	
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
	
	public void clearData();
	public void populateDatabase();
	
	// for testing purposes
	public void withdrawFailWithRollback(Account account, double value);
}
