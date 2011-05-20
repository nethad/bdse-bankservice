package ch.uzh.ejb.bank;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.security.auth.callback.UsernamePasswordHandler;

import ch.uzh.ejb.bank.entities.Customer;
import ch.uzh.ejb.bank.entities.Customer.Gender;

public class TestClient {
	
	private static LoginContext loginContext = null;
	
	public static void login(String username, String password)
	throws LoginException {
		UsernamePasswordHandler handler =
			new UsernamePasswordHandler(username, password.toCharArray());
		loginContext = new LoginContext("ba", handler);
		loginContext.login();
	}
	
	public static void logout()
	throws LoginException {
		loginContext.logout();
	}
	
	public static void main(String[] args) {
		try {
			Properties props=new Properties();
			props.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
			props.put("java.naming.provider.url", "localhost:1099");
//			props.put("login.configuration.provider", "org.jboss.security.auth.login.XMLLoginConfig");
			
			Context ctx=new InitialContext(props);
			login("admin", "admin");
			BankApplicationRemote bankService = (BankApplicationRemote)ctx.lookup("BankApplication/remote");
			
//			bankService.createAccount(1000.0D, Type.PRIVAE_CREDIT, 1.0F, -1000.0D, customer);
			Customer customer = bankService.createCustomer("test", "test", "F", "L", "", Gender.MALE, "CH");
			System.out.println("Customer: "+customer);
//			List<Quote> quotes = new ArrayList<Quote>();
//			quotes.add(new Quote("Quote 1a"));
//			quotes.add(new Quote("Quote 2a"));
//			quoteService.addQuotes(quotes);
//			
//			quoteService.addQuote("So long and thanks for all the fish!");
//			for(Quote q : quoteService.findQuote("%a%m%")) {
//				System.out.println(q.getSource().getName()+": "+q.getText());
//			}
//			System.out.println(quoteService.getQuote());
			bankService.remove();
		} catch(Throwable e) {
			e.printStackTrace();
			System.err.println("Caused by: ");
			e.getCause().printStackTrace();
		}
	}

}
