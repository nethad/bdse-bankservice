package ch.uzh.ejb.bank.client;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.security.auth.callback.UsernamePasswordHandler;

import ch.uzh.ejb.bank.BankApplicationRemote;
import ch.uzh.ejb.bank.BankApplicationTestRemote;
import ch.uzh.ejb.bank.client.commands.AbstractCommandHandler;
import ch.uzh.ejb.bank.client.commands.AccountHistoryCommandHandler;
import ch.uzh.ejb.bank.client.commands.AddShareCommandHandler;
import ch.uzh.ejb.bank.client.commands.CreateAccountCommandHandler;
import ch.uzh.ejb.bank.client.commands.CreateCustomerCommandHandler;
import ch.uzh.ejb.bank.client.commands.DepositCommandHandler;
import ch.uzh.ejb.bank.client.commands.GetAccountsCommandHandler;
import ch.uzh.ejb.bank.client.commands.GetAllAccountsCommandHandler;
import ch.uzh.ejb.bank.client.commands.GetAllCustomersCommandHandler;
import ch.uzh.ejb.bank.client.commands.GetPortfolioCommandHandler;
import ch.uzh.ejb.bank.client.commands.HelpCommandHandler;
import ch.uzh.ejb.bank.client.commands.LoginCommandHandler;
import ch.uzh.ejb.bank.client.commands.MortgageProcessCommandHandler;
import ch.uzh.ejb.bank.client.commands.SelectAccountCommandHandler;
import ch.uzh.ejb.bank.client.commands.SelectCustomerCommandHandler;
import ch.uzh.ejb.bank.client.commands.ShowAccountCommandHandler;
import ch.uzh.ejb.bank.client.commands.ShowCustomerCommandHandler;
import ch.uzh.ejb.bank.client.commands.StatusCommandHandler;
import ch.uzh.ejb.bank.client.commands.TransferCommandHandler;
import ch.uzh.ejb.bank.client.commands.TransferShareCommandHandler;
import ch.uzh.ejb.bank.client.commands.WithdrawCommandHandler;

import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.SimpleCompletor;

public class CommandLineReader implements BankApplicationProvider {

	private ConsoleReader reader;
	private BankApplicationRemote bankApplication;
	private ArrayList<AbstractCommandHandler> commandHandlers;

	public CommandLineReader() throws IOException, NamingException {
		setupConsoleReader();
		setupWebServiceBinding();
		setupCommandHandlers();
		addCommandsToCompletor();
	}

	private void setupCommandHandlers() {
		this.commandHandlers = new ArrayList<AbstractCommandHandler>();
		this.commandHandlers.add(new HelpCommandHandler(this));
		this.commandHandlers.add(new CreateAccountCommandHandler(this));
		this.commandHandlers.add(new CreateCustomerCommandHandler(this));
		this.commandHandlers.add(new LoginCommandHandler(this));
		this.commandHandlers.add(new GetAccountsCommandHandler(this));
		this.commandHandlers.add(new GetAllAccountsCommandHandler(this));
		this.commandHandlers.add(new GetAllCustomersCommandHandler(this));
		this.commandHandlers.add(new SelectCustomerCommandHandler(this));
		this.commandHandlers.add(new SelectAccountCommandHandler(this));
		this.commandHandlers.add(new ShowAccountCommandHandler(this));
		this.commandHandlers.add(new ShowCustomerCommandHandler(this));
		
		this.commandHandlers.add(new StatusCommandHandler(this));
		this.commandHandlers.add(new AccountHistoryCommandHandler(this));
		
		this.commandHandlers.add(new DepositCommandHandler(this));
		this.commandHandlers.add(new WithdrawCommandHandler(this));
		this.commandHandlers.add(new TransferCommandHandler(this));
		
		this.commandHandlers.add(new AddShareCommandHandler(this));
		this.commandHandlers.add(new GetPortfolioCommandHandler(this));
		this.commandHandlers.add(new TransferShareCommandHandler(this));
		
		this.commandHandlers.add(new MortgageProcessCommandHandler(this));
	}

	private void addCommandsToCompletor() {
		List<Completor> completors = new LinkedList<Completor>();
		List<String> commands = new ArrayList<String>();
		for(AbstractCommandHandler handler : this.commandHandlers) {
			commands.add(handler.getCommand());
		}
		completors.add(new SimpleCompletor(
				commands.toArray(new String[]{})));
		reader.addCompletor(new ArgumentCompletor(completors));
	}

	private void setupWebServiceBinding() throws NamingException {
		Properties props = new Properties();
		props.put("java.naming.factory.initial",
				"org.jnp.interfaces.NamingContextFactory");
		props.put("java.naming.provider.url", "localhost:1099");
		InitialContext context = new InitialContext(props);
		bankApplication = (BankApplicationTestRemote) context
				.lookup("BankApplicationTestBean/remote");
	}

	private void setupConsoleReader() throws IOException {
		reader = new ConsoleReader();
		reader.setBellEnabled(false);
		reader.setDebug(new PrintWriter(new FileWriter("writer.debug", true)));
	}

	public void start() throws IOException {
		String line;
//		PrintWriter out = new PrintWriter(System.out);

		while ((line = reader.readLine("bankservice> ")) != null) {
//			out.println("==> " + line);
//			out.flush();
			String normalizedLine = line.toLowerCase().trim();

			if (isExitCommand(normalizedLine)) {
				break;
			} else if (isBlankLine(normalizedLine)) {
				continue;
			} else {
				parseLine(line.trim()); // no lower-casing intentional
			}
		}
	}

	private boolean isBlankLine(String normalizedLine) {
		return normalizedLine.equals("");
	}

	private boolean isExitCommand(String line) {
		return line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit");
	}

	private void parseLine(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line);
		if (tokenizer.hasMoreTokens()) {
			dispatchCommand(tokenizer);
		}
	}

	private void dispatchCommand(StringTokenizer tokenizer) {
		try {
			chooseHandler(tokenizer);
		} catch (Exception e) {
			System.err.println("[ERROR] Command unsuccessful: "+e.getMessage());
		}
	}
	
	private void chooseHandler(StringTokenizer tokenizer) throws Exception {
		String firstToken = tokenizer.nextToken();
		boolean foundHandler = false;
		for (AbstractCommandHandler handler : this.commandHandlers) {
			if (handler.getCommand().equals(firstToken)) {
				executeHandler(tokenizer, handler);
				foundHandler = true;
				break;
			}
		}
		if (foundHandler == false) {
			System.err.println("[ERROR] Unknown command: "+firstToken);
		}
	}

	private void executeHandler(StringTokenizer tokenizer,
			AbstractCommandHandler handler) throws Exception {
		try {
			handler.execute(tokenizer);
		} catch (NoSuchElementException e) {
			throw new Exception("Not enough arguments for "+
					handler.getCommand()+" command.\n"+
					handler.getUsage());
		}
	}

	public static void login(String username, String password)
			throws LoginException {
		UsernamePasswordHandler handler = new UsernamePasswordHandler(username,
				password.toCharArray());
		LoginContext loginContext = new LoginContext("ba", handler);
		loginContext.login();
	}

	@Override
	public BankApplicationRemote getBankApplication() {
		if (this.bankApplication == null) {
			throw new RuntimeException("Web service binding not established.");
		}
		return this.bankApplication;
	}

	public void printHelp() {
		for (AbstractCommandHandler handler : this.commandHandlers) {
			System.out.println(handler.getUsage());
		}
	}
	
	public void printHelpFor(String command) {
		for (AbstractCommandHandler handler : this.commandHandlers) {
			if (handler.getCommand().equals(command)) {
				System.out.println(handler.getUsage());				
			}
		}
	}

}
