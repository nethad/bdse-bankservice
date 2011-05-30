package ch.uzh.ejb.bank.client.commands;

import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;

public class GetNetChange12MonthsCommandHandler extends AbstractCommandHandler {
	public GetNetChange12MonthsCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}
	
	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		Date to = new Date();
		
		System.out.println("Net Change from: " + cal.getTime().toString() + " to " + 
				to.toString() + ": " + getBankApplication().getNetChange(cal.getTime(), to));
	}

	@Override
	public String getUsage() {
		return getCommand();
	}

	@Override
	public String getCommand() {
		return "get_net_change12m";
	}
}
