package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;
import ch.uzh.ejb.bank.entities.Customer;

public class ShowCustomerCommandHandler extends AbstractCommandHandler {

	public ShowCustomerCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		long customerId = Long.parseLong(tokenizer.nextToken());
		Customer customer = getBankApplication().getCustomer(customerId);
		printCustomer(customer);
	}

	private void printCustomer(Customer customer) {
		PrintHelper.printElementsWithTab(
				"id", "first_name", "last_name", "user_name",
				"address", "gender", "nationality");
		PrintHelper.printElementsWithTab(
				customer.getCustomerId(),
				customer.getFirstName(),
				customer.getLastName(), 
				customer.getUserName(),
				customer.getAddress(),
				customer.getGender(), 
				customer.getNationality());
		System.out.println("===");
	}

	@Override
	public String getUsage() {
		return getCommand()+" [customer id]";
	}

	@Override
	public String getCommand() {
		return "show_customer";
	}

}
