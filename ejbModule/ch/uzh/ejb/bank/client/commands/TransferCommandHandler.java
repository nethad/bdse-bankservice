package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;

public class TransferCommandHandler extends AbstractCommandHandler {

	public TransferCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		long targetAccountId = Long.parseLong(tokenizer.nextToken());
		double amount = Double.parseDouble(tokenizer.nextToken());
		getBankApplication().transfer(targetAccountId, amount);
	}

	@Override
	public String getUsage() {
		return getCommand()+" [target account id] [amount]";
	}

	@Override
	public String getCommand() {
		return "transfer";
	}

}
