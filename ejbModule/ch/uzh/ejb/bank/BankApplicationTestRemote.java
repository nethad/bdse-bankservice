package ch.uzh.ejb.bank;

import javax.ejb.Remote;

import ch.uzh.ejb.bank.entities.Account;

/**
 * Interface for Unit-Test access.
 * 
 * @author daniel
 *
 */
@Remote
public interface BankApplicationTestRemote extends BankApplicationRemote {
	public void withdrawFailWithRollback(Account account, double value);
	public boolean isInRole(String role);
}
