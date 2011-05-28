package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;

public class SelectCustomerCommandHandler extends AbstractCommandHandler {

	public SelectCustomerCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		Long id = Long.parseLong(tokenizer.nextToken());
		getBankApplication().selectCustomer(id);
		System.out.println("Selected customer id: "+
				getBankApplication().getSelectedCustomerId());
	}

	@Override
	public String getUsage() {
		return getCommand()+" [id]";
	}

	@Override
	public String getCommand() {
		return "select_customer";
	}

}
