package ch.uzh.ejb.bank.client;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.entities.Account.Type;

public class CreateAccountCommandHandler extends AbstractCommandHandler {

	public CreateAccountCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	public void execute(StringTokenizer tokenizer) {
		double balance = Double.parseDouble(tokenizer.nextToken());
		getBankApplication().createAccount(
				balance, Type.PRIVAE_CREDIT, 1.25F, -1000.0, null);
	}

	@Override
	public String getUsage() {
		return getCommand()+" [balance]";
	}

	@Override
	public String getCommand() {
		return "create_account";
	}

}
