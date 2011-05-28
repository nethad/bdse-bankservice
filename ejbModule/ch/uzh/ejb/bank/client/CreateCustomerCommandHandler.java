package ch.uzh.ejb.bank.client;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import ch.uzh.ejb.bank.entities.Customer.Gender;

public class CreateCustomerCommandHandler extends AbstractCommandHandler {

	public CreateCustomerCommandHandler(BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	public void execute(StringTokenizer tokenizer) throws Exception {
		try {
			String username = tokenizer.nextToken();
			String password = tokenizer.nextToken();
			
			createCustomer(username, password);
			
//			login(username, password);
		} catch (NoSuchElementException e) {
			throw new Exception("Not enough arguments for create_customer command.");
		}
	}

	private void createCustomer(String username, String password) {
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
