package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;

public class SelectAccountCommandHandler extends AbstractCommandHandler {

	public SelectAccountCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		Long id = Long.parseLong(tokenizer.nextToken());
		getBankApplication().selectAccount(id);
		System.out.println("Selected account id: "+
				getBankApplication().getSelectedAccountId());
	}

	@Override
	public String getUsage() {
		return getCommand()+" [id]";
	}

	@Override
	public String getCommand() {
		return "select_account";
	}

}
