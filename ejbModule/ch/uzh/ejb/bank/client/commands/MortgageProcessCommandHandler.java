package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;
import ch.uzh.ejb.bank.client.MortgageProcessImpl;
import ch.uzh.ejb.bank.process.MortgageProcess;

public class MortgageProcessCommandHandler extends AbstractCommandHandler {

	public MortgageProcessCommandHandler(BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}
	
	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		MortgageProcess mortgageProcess = new MortgageProcessImpl(getBankApplication(), 
				getBankApplication().getSelectedCustomerId(), 
				getBankApplication().getSelectedAccountId());
		mortgageProcess.execute();
	}

	@Override
	public String getUsage() {
		return getCommand();
	}

	@Override
	public String getCommand() {
		return "mortgageprocess";
	}

}
