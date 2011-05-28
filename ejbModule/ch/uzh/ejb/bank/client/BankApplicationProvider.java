package ch.uzh.ejb.bank.client;

import ch.uzh.ejb.bank.BankApplicationRemote;

public interface BankApplicationProvider {
	
	public BankApplicationRemote getBankApplication();

}
