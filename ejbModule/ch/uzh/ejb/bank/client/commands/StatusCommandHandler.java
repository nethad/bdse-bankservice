package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;

public class StatusCommandHandler extends AbstractCommandHandler {

	public StatusCommandHandler(BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		long selectedCustomerId = getBankApplication().getSelectedCustomerId();
		long selectedAccountId = getBankApplication().getSelectedAccountId();
		System.out.println("selected customer: "+selectedCustomerId);
		System.out.println("selected account: "+selectedAccountId);
	}

	@Override
	public String getUsage() {
		return getCommand();
	}

	@Override
	public String getCommand() {
		return "status";
	}

}
