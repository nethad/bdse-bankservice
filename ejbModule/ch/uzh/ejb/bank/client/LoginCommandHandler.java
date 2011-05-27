package ch.uzh.ejb.bank.client;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.security.auth.callback.UsernamePasswordHandler;

public class LoginCommandHandler {

	private CommandLineReader commandLineReader;
	private StringTokenizer tokenizer;

	public LoginCommandHandler(CommandLineReader commandLineReader,
			StringTokenizer tokenizer) {
		this.commandLineReader = commandLineReader;
		this.tokenizer = tokenizer;
	}

	public void execute() throws Exception {
		try {
			String username = this.tokenizer.nextToken();
			String password = this.tokenizer.nextToken();
			
			login(username, password);
		} catch (NoSuchElementException e) {
			throw new Exception("Not enough arguments for login command.");
		}
	}

	public void login(String username, String password) throws LoginException {
		UsernamePasswordHandler handler = new UsernamePasswordHandler(username,
				password.toCharArray());
		LoginContext loginContext = new LoginContext("ba", handler);
		loginContext.login();
	}

}
