package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;
import ch.uzh.ejb.bank.entities.Account;

public class ShowAccountCommandHandler extends AbstractCommandHandler {

	public ShowAccountCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		long accountNumber = Long.parseLong(tokenizer.nextToken());
		Account account = getBankApplication().getAccount(accountNumber);
		printAccount(account);
	}

	private void printAccount(Account account) {
		PrintHelper.printElementsWithTab(
				"id", "status", "type\t", "balance", 
				"credit_limit", "customer_id", "interest");
		PrintHelper.printElementsWithTab(
				account.getAccountId(),
				account.getAccountStatus(),
				account.getAccountType(),
				account.getBalance(),
				account.getCreditLimit(),
				account.getCustomer().getCustomerId(),
				account.getInterest());
		System.out.println("===");
	}

	@Override
	public String getUsage() {
		return getCommand()+" [account number]";
	}

	@Override
	public String getCommand() {
		return "show_account";
	}

}
