package ch.uzh.ejb.bank.client;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.BankApplicationRemote;

public abstract class AbstractCommandHandler {

	private BankApplicationProvider bankApplicationProvider;

	public AbstractCommandHandler(BankApplicationProvider bankApplicationProvider) {
		this.bankApplicationProvider = bankApplicationProvider;
	}
	
	protected BankApplicationRemote getBankApplication() {
		return bankApplicationProvider.getBankApplication();
	}
	
	public abstract void execute(StringTokenizer tokenizer) throws Exception;
	
	public abstract String getUsage();
	
	public abstract String getCommand();

}
