package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;

public class AddShareCommandHandler extends AbstractCommandHandler {

	public AddShareCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		String symbol = tokenizer.nextToken();
		long quantity = Long.parseLong(tokenizer.nextToken());
		double purchasePrice = Double.parseDouble(tokenizer.nextToken());
		getBankApplication().addShare(symbol, quantity, purchasePrice);
	}

	@Override
	public String getUsage() {
		return getCommand()+" [symbol] [quantity] [purchase price]";
	}

	@Override
	public String getCommand() {
		return "add_share";
	}

}
