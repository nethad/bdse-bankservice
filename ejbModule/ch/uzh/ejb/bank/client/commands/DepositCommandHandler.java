package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;

public class DepositCommandHandler extends AbstractCommandHandler {

	public DepositCommandHandler(BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		double amount = Double.parseDouble(tokenizer.nextToken());
		getBankApplication().deposit(amount);
	}

	@Override
	public String getUsage() {
		return getCommand()+" [amount]";
	}

	@Override
	public String getCommand() {
		return "deposit";
	}

}
