package ch.uzh.ejb.bank.client.commands;

import java.util.List;
import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;
import ch.uzh.ejb.bank.entities.Account;

public class GetAccountsCommandHandler extends AbstractCommandHandler {

	public GetAccountsCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		List<Account> accounts = getBankApplication().getAccounts();
		printList(accounts);
	}

	private void printList(List<Account> accounts) {
		System.out.println("=== Accounts ("+accounts.size()+")");
		System.out.println("id\tbalance");
		for (Account account : accounts) {
			System.out.println(account.getBalance()+"\t"+
					account.getBalance());
		}
		System.out.println("===");
	}

	@Override
	public String getUsage() {
		return getCommand();
	}

	@Override
	public String getCommand() {
		return "get_accounts";
	}

}
