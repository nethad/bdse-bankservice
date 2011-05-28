package ch.uzh.ejb.bank.client.commands;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.security.auth.callback.UsernamePasswordHandler;

import ch.uzh.ejb.bank.client.BankApplicationProvider;

public class LoginCommandHandler extends AbstractCommandHandler {

	public LoginCommandHandler(BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	public void execute(StringTokenizer tokenizer) throws Exception {
		try {
			String username = tokenizer.nextToken();
			String password = tokenizer.nextToken();
			
			login(username, password);
		} catch (NoSuchElementException e) {
			throw new Exception("Not enough arguments for login command.\n"+
					getUsage());
		}
	}

	public void login(String username, String password) throws LoginException {
		UsernamePasswordHandler handler = new UsernamePasswordHandler(username,
				password.toCharArray());
		LoginContext loginContext = new LoginContext("ba", handler);
		loginContext.login();
		getBankApplication().setDefaultCustomer();
	}

	@Override
	public String getUsage() {
		return getCommand()+" [username] [password]";
	}

	@Override
	public String getCommand() {
		return "login";
	}

}
