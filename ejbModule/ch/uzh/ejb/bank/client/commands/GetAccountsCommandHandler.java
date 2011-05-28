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
		PrintHelper.printElementsWithTab("id", "balance", "customer_id");
		for (Account account : accounts) {
			PrintHelper.printElementsWithTab(
					account.getAccountId(),
					account.getBalance(),
					account.getCustomer().getCustomerId());
		}
		System.out.println("===");
	}

	@Override
	public String getUsage() {
		return getCommand();
	}

	@Override
	public String getCommand() {
		return "accounts";
	}

}
