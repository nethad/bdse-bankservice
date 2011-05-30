package ch.uzh.ejb.bank.client.commands;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.text.DateFormatter;

import ch.uzh.ejb.bank.client.BankApplicationProvider;

public class AccountHistoryCommandHandler extends AbstractCommandHandler {

	private static final String DATE_FORMAT = "yyyy-MM-dd";

	public AccountHistoryCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		String fromDateString = tokenizer.nextToken();
		String toDateString = tokenizer.nextToken();
		
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
	    Date fromDate = (Date)formatter.parse(fromDateString);
	    Date toDate = (Date)formatter.parse(toDateString);
		
		String accountHistory = getBankApplication().getAccountHistory(fromDate, toDate);
		System.out.println(accountHistory);
	}

	@Override
	public String getUsage() {
		return getCommand()+" [from date] [to date] (format: YYYY-MM-DD)";
	}

	@Override
	public String getCommand() {
		return "account_history";
	}

}
