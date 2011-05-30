package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;

public class TransferShareCommandHandler extends AbstractCommandHandler {

	public TransferShareCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		long buyerAccountId = Long.parseLong(tokenizer.nextToken());
		String symbol = tokenizer.nextToken();
		long quantity = Long.parseLong(tokenizer.nextToken());
		double price = Double.parseDouble(tokenizer.nextToken());
		getBankApplication().transferShare(buyerAccountId, symbol, quantity, price);
	}

	@Override
	public String getUsage() {
		return getCommand()+" [receiver accountId] [symbol] [quantity] [price]";
	}

	@Override
	public String getCommand() {
		return "transfer_share";
	}

}
