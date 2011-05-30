package ch.uzh.ejb.bank;
import java.util.Date;
import java.util.List;

import javax.ejb.Remote;

import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;
import ch.uzh.ejb.bank.entities.Portfolio;
import ch.uzh.ejb.bank.process.MortgageApplication;

/**
 * BDSE Bank Application by Thomas Ritter and Daniel Spicar.
 * 
 * @author daniel, thomas
 *
 */
@Remote
public interface BankApplicationRemote {
	/**
	 * Remove the Session bean.
	 */
	public void remove();
	
	/**
	 * Create a new customer.
	 * 
	 * @param userName	user name used for log in
	 * @param password	password used for log in
	 * @param firstName	first name
	 * @param lastName	last name
	 * @param address	address
	 * @param gender	gender
	 * @param nationality	nationality
	 * @return	the new customer object.
	 */
	public Customer createCustomer(String userName, String password, String firstName, String lastName, String address, 
			Customer.Gender gender, String nationality) throws Exception;
	
	/**
	 * Find customers by first and last name.
	 * @param firstName	first name
	 * @param lastName	last name
	 * @return A list of all customers with given first and last name.
	 */
	public List<Customer> getCustomer(String firstName, String lastName);
	
	/**
	 * Get a List of all customers.
	 * 
	 * @return a list containing all customers.
	 */
	public List<Customer> getAllCustomers();
	
	/**
	 * Get customer by id.
	 * 
	 * @param id	customer id/number
	 * @return	the customer with given id or null.
	 */
	public Customer getCustomer(long id);
	
	/**
	 * Update customer information persistently given a customer object.
	 * 
	 * @param customer the customer object.
	 */
	public void updateCustomer(Customer customer);
	
	/**
	 * Selects the logged in user, should be called after login. 
	 */
	public void selectLoggedInUser();
	
	/**
	 * Create a new account for the selected customer.
	 * 
	 * @param balance	the initial balance.
	 * @param accountType	the type of account.
	 * @param interest	the interest rate.
	 * @param creditLimit	the credit limit.
	 * @return	the new account object.
	 */
	public Account createAccount(double balance, Account.Type accountType,
			float interest, double creditLimit) throws Exception;
	
	/**
	 * Get account by id/account number.
	 * 
	 * @param accountNumber	the account id.
	 * @return	the account object or null.
	 */
	public Account getAccount(long accountNumber);
	
	/**
	 * Returns all accounts of the selected user.
	 * 
	 * @return	all accounts of the selected user.
	 */
	public List<Account> getSelectedUserAccounts() throws Exception;
	
	/**
	 * Returns all accounts.
	 * 
	 * @return all accounts
	 */
	public List<Account> getAllAccounts();
	
	/**
	 * Set the account status of the selected account to open or closed.
	 * 
	 * @param accountId	the account id
	 * @param status	open/closed
	 */
	public void setAccountStatus(Account.Status status) throws Exception;
	
	/**
	 * Deposit an amount on the selected account.
	 * 
	 * @param value	the amount to deposit.
	 * @return	the updated account object.
	 */
	public Account deposit(double value) throws Exception;
	
	/**
	 * Withdraw an amount from a the selected account.
	 * 
	 * @param value	the amount to withdraw.
	 * @return	the updated account object.
	 */
	public Account withdraw(double value) throws Exception;
	
	/**
	 * Transfer an amount from the selected account to another account.
	 * 
	 * @param targetAccountId	the receiving account
	 * @param value	the amount
	 */
	void transfer(long targetAccountId, double value) throws Exception; // stateful
	
	/**
	 * Get the history of the selected account.
	 * 
	 * @param from consider events from this date on.
	 * @param to	consider events until this date.
	 * @return	the account history.
	 */
	public String getAccountHistory(Date from, Date to) throws Exception; // stateful
	
	/**
	 * Get the cumulated balance of all accounts of the selected user.
	 * 
	 * @return the cumultaed balance
	 */
	public double getTotalBalance() throws Exception;
	
	/**
	 * Get income of the selected customer.
	 * 
	 * @param from consider events from this date on.
	 * @param to	consider events until this date.
	 * @return	the selected customer's income in the specified timeframe.
	 */
	public double getIncome(Date from, Date to) throws Exception;
	
	/**
	 * Get net account balance change of the selected customer.
	 * 
	 * @param from consider events from this date on.
	 * @param to	consider events until this date.
	 * @return the selected customer's balance net change in the specified timeframe.
	 */
	public double getNetChange(Date from, Date to) throws Exception;
	
	/**
	 * Select an account as target of operations.
	 * 
	 * @param id the account id.
	 */
	public void selectAccount(long id) throws Exception;
	
	/**
	 * Get the selected account. 
	 * 
	 * @return	the selected account's id/number.
	 */
	public long getSelectedAccountId() throws Exception;
	
	/**
	 * Select a customer as target of operations.
	 * 
	 * @param id	the customer id
	 */
	public void selectCustomer(long id);
	
	/**
	 * Get the selected customer.
	 * 
	 * @return	the selected customer's id.
	 */
	public long getSelectedCustomerId() throws Exception;
	
	/**
	 * Pay out a mortgage.
	 * 
	 * @param mortgageApplication	the mortgage application.
	 */
	public void payOutMortgage(MortgageApplication mortgageApplication) throws Exception;
	
	/**
	 * Get the selected user's portfolio.
	 * 
	 * @return	the portfolio.
	 */
	public Portfolio getCustomerPortfolio() throws Exception;
	
	/**
	 * Add shares of a specific type to the selected user's portfolio.
	 * 
	 * @param symbol	the symbol identifying the portfolio.
	 * @param quantity	how many share's to add
	 * @param purchasePrice	the purchase price.
	 */
	public void addShare(String symbol, long quantity, double purchasePrice) throws Exception;
	
	/**
	 * Remove shares from the selected user's portfolio.
	 * 
	 * @param symbol	the symbol identifying the portfolio.
	 * @param quantity	the amount of shares to remove.
	 */
	public void removeShare(String symbol, long quantity) throws Exception;
	
	/**
	 * Transfer shares from the selected user's portfolio to the portfolio of the owner of the receiving (buyer) account.
	 * The money is removed from the specified buyeraccount and added to the selected account.
	 * 
	 * @param buyerAccountId	the account that the money get subtracted from. It's owner receives the shares.
	 * @param symbol	the type of shares to transfer.
	 * @param quantity	the quantity of shares to transfer.
	 * @param price	the price for the shares.
	 */
	public void transferShare(long buyerAccountId, String symbol, long quantity, double price) throws Exception;
}
