package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;
import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Account.Type;

public class CreateAccountCommandHandler extends AbstractCommandHandler {

	public CreateAccountCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	public void execute(StringTokenizer tokenizer) throws Exception {
		double balance = Double.parseDouble(tokenizer.nextToken());
		Account account = getBankApplication().createAccount(
				balance, Type.PRIVAE_CREDIT, 1.25F, -1000.0);
		long accountId = account.getAccountId();
		getBankApplication().selectAccount(accountId);
		System.out.println("Account "+accountId+" created and selected.");
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
