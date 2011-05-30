package ch.uzh.ejb.bank;

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
	
}
