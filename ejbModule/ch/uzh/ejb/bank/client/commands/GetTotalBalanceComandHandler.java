package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;

public class GetTotalBalanceComandHandler extends AbstractCommandHandler {

	public GetTotalBalanceComandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}
	
	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		System.out.println("Total Balance: " + getBankApplication().getTotalBalance());
	}

	@Override
	public String getUsage() {
		return getCommand();
	}

	@Override
	public String getCommand() {
		return "get_total_balance";
	}

}
