package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;

public class WithdrawCommandHandler extends AbstractCommandHandler {

	public WithdrawCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		double amount = Double.parseDouble(tokenizer.nextToken());
		getBankApplication().withdraw(amount);
	}

	@Override
	public String getUsage() {
		return getCommand()+" [amount]";
	}

	@Override
	public String getCommand() {
		return "withdraw";
	}

}
