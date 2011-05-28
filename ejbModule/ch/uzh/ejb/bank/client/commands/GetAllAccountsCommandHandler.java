package ch.uzh.ejb.bank.client.commands;

import java.util.List;
import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;
import ch.uzh.ejb.bank.entities.Account;

public class GetAllAccountsCommandHandler extends AbstractCommandHandler {

	public GetAllAccountsCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		List<Account> accounts = getBankApplication().getAllAccounts();
		printList(accounts);
	}

	private void printList(List<Account> accounts) {
		System.out.println("=== Accounts ("+accounts.size()+")");
		System.out.println("id\tbalance\tcustomer_id");
		for (Account account : accounts) {
			System.out.println(account.getAccountId()+"\t"+
					account.getBalance()+"\t"+
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
		return "get_all_accounts";
	}

}
