package ch.uzh.ejb.bank;
import java.util.Date;
import java.util.List;

import javax.ejb.Remote;

import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;
import ch.uzh.ejb.bank.entities.Portfolio;
import ch.uzh.ejb.bank.process.MortgageApplication;

@Remote
public interface BankApplicationRemote {
	public void remove();
	
	public Customer createCustomer(String userName, String password, String firstName, String lastName, String address, 
			Customer.Gender gender, String nationality);
	public List<Customer> getCustomer(String firstName, String lastName);
	public List<Customer> getAllCustomers();
	public Customer getCustomer(long id);
	
	public void setDefaultCustomer(); // should be invoked after login
	
	public Account createAccount(double balance, Account.Type accountType,
			float interest, double creditLimit) throws Exception; // now stateful
	public Account getAccount(long accountNumber); 
	public List<Account> getAccounts(Customer customer);
	public List<Account> getAccounts() throws Exception; // stateful
	public List<Account> getAllAccounts();
	public void setAccountStatus(Account account, Account.Status status) throws Exception;
	
	public Account deposit(Account toAccount, double value) throws Exception;
	public Account deposit(double value) throws Exception; // stateful
	public Account withdraw(Account fromAccount, double value) throws Exception;
	public Account withdraw(double value) throws Exception; // stateful
	public void transfer(Account fromAccount, Account toAccount, double value) throws Exception;
	void transfer(long targetAccountId, double value) throws Exception; // stateful
	
	public String getAccountHistory(Account account, Date from, Date to) throws Exception;
	public String getAccountHistory(Date from, Date to) throws Exception; // stateful
	
	public double getTotalBalance(Customer customer) throws Exception;
	public double getIncome(Customer customer, Date from, Date to) throws Exception;
	public double getNetChange(Customer customer, Date from, Date to) throws Exception;
	
	// stateful stuff
	public void selectAccount(long id) throws Exception;
	public long getSelectedAccountId() throws Exception;
	public void selectCustomer(long id);
	public long getSelectedCustomerId() throws Exception;
	
	// session experiment
	public void test();
	
	//for mortgageprocess
	public void payOutMortgage(MortgageApplication mortgageApplication) throws Exception;
	public void updateCustomer(Customer customer);
	
	//portfolio mgmt
	public Portfolio createPortfolio() throws Exception;
	public Portfolio getCustomerPortfolio() throws Exception;
	public void addShare(String symbol, long quantity, double purchasePrice) throws Exception;
	public void removeShare(String symbol, long quantity) throws Exception;
	public void transferShare(long buyerAccountId, String symbol, long quantity, double price) throws Exception;
}
