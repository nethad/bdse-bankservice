package ch.uzh.ejb.bank;

import static org.junit.Assert.*;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Base Test class for all BankApplication Unit Tests.
 * 
 * Offers some utility methods and environment set-up.
 * 
 * Note: The Application Server needs to be running for these tests to work.
 * 
 * @author daniel
 *
 */
public abstract class BankApplicationBaseTestCase {

	static LoginContext loginContext = null;
	static Context context;
	static BankApplicationRemote bankApplication;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Properties props = new Properties();
		props.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		props.put("java.naming.provider.url", "localhost:1099");
		
		context = new InitialContext(props);

		bankApplication = (BankApplicationRemote) context.lookup("BankApplication/remote");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		bankApplication.remove();
	}
	
	/**
	 * Create an empty account for the supplied customer.
	 */
	Account createDefaultAccount(Customer customer) {
		return createAccount(customer, 0.0);
	}
	
	/**
	 * Create an account with for the specified customer with the specified balance.
	 */
	Account createAccount(Customer customer, double balance) {
		Account account = bankApplication.createAccount(balance, Account.Type.PRIVATE_DEBIT, 1.25f, -1000.0, customer);
		return account;
	}

	/**
	 * Create a customer with given first and last name. 
	 * The user name will be the first name and the password '1111'.
	 */
	Customer createCustomer(String firstName, String lastName) {
		Customer customer = bankApplication.createCustomer(firstName, "1111", firstName, lastName, "Lustiggasse 5, " +
				"9999 Lustigburg", Customer.Gender.MALE, "CH");
		return customer;
	}
	
	Customer getDefaultUserCustomer() {
		return bankApplication.getCustomer(102);
	}
	
	void reloadBankApplicationBean() {
		try {
			bankApplication = (BankApplicationRemote) context.lookup("BankApplication/remote");
		} catch (NamingException e) {
			fail("Could not reload bank application: "+e.getMessage());
		}
	}
	
	void loginAsUser() {
		String login = "user";
		try {
			login(login, login);
		} catch (LoginException e) {
			fail("Could not login as "+login+": "+e.getMessage());
		}
	}
	
	void loginAsClerk() {
		String login = "clerk";
		try {
			login(login, login);
		} catch (LoginException e) {
			fail("Could not login as "+login+": "+e.getMessage());
		}
	}
	
	void loginAsAdmin() {
		String login = "admin";
		try {
			login(login, login);
		} catch (LoginException e) {
			fail("Could not login as "+login+": "+e.getMessage());
		}
	}
	
	public static void login(String username, String password) throws LoginException {
		UsernamePasswordHandler handler =
			new UsernamePasswordHandler(username, password.toCharArray());
		loginContext = new LoginContext("ba", handler);
		loginContext.login();
	}
	
	public static void logout() throws LoginException {
		loginContext.logout();
	}
}
