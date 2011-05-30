package ch.uzh.ejb.bank;

import java.util.Date;
import java.util.List;

import javax.ejb.Remote;

import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;

/**
 * Interface for Unit-Test access.
 * 
 * @author daniel
 *
 */
@Remote
public interface BankApplicationTestRemote extends BankApplicationRemote {
	
	public void withdrawFailWithRollback(Account account, double value) throws Exception;
	public boolean isInRole(String role);
	public Customer getCustomerByUsername(String userName);
	public List<Account> getAccounts(Customer customer);
	public Account deposit(Account toAccount, double value) throws Exception;
	public Account withdraw(Account fromAccount, double value) throws Exception;
	public void transfer(Account fromAccount, Account toAccount, double value) throws Exception;
	public String getAccountHistory(Account account, Date from, Date to) throws Exception;
	
}
