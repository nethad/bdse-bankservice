package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;
import ch.uzh.ejb.bank.entities.Customer.Gender;

public class CreateCustomerCommandHandler extends AbstractCommandHandler {

	public CreateCustomerCommandHandler(BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	public void execute(StringTokenizer tokenizer) throws Exception {
			String username = tokenizer.nextToken();
			String password = tokenizer.nextToken();
			
			createCustomer(username, password);
	}

	private void createCustomer(String username, String password) throws Exception {
		getBankApplication().createCustomer(
				username, password, "Firstname", "Lastname", "", Gender.OTHER, "None");
	}

	@Override
	public String getUsage() {
		return getCommand()+" [username] [password]";
	}

	@Override
	public String getCommand() {
		return "create_customer";
	}

}
