package ch.uzh.ejb.bank.client;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class CreateCustomerCommandHandler {
	
	private CommandLineReader commandLineReader;
	private StringTokenizer tokenizer;

	public CreateCustomerCommandHandler(CommandLineReader commandLineReader,
			StringTokenizer tokenizer) {
		this.commandLineReader = commandLineReader;
		this.tokenizer = tokenizer;
	}

	public void execute() throws Exception {
		try {
			String username = this.tokenizer.nextToken();
			String password = this.tokenizer.nextToken();
			
//			login(username, password);
		} catch (NoSuchElementException e) {
			throw new Exception("Not enough arguments for create_customer command.");
		}
	}

}
