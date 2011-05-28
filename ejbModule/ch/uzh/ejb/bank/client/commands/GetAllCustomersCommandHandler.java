package ch.uzh.ejb.bank.client.commands;

import java.util.List;
import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;
import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;

public class GetAllCustomersCommandHandler extends AbstractCommandHandler {

	public GetAllCustomersCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		List<Customer> customers = getBankApplication().getAllCustomers();
		printList(customers);
	}

	private void printList(List<Customer> customers) {
		System.out.println("=== Customers ("+customers.size()+")");
		System.out.println("id\tusername");
		for (Customer customer : customers) {
			System.out.println(customer.getCustomerId()+"\t"+
					customer.getUserName());
		}
		System.out.println("===");
	}

	@Override
	public String getUsage() {
		return getCommand();
	}

	@Override
	public String getCommand() {
		return "get_all_customers";
	}

}
