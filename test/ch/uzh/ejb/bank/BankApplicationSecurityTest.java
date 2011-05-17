package ch.uzh.ejb.bank;

import static org.junit.Assert.*;

import java.util.Properties;

import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.uzh.ejb.bank.Account.Type;
import ch.uzh.ejb.bank.Customer.Gender;

/**
 * For security to work, the following key value pair must be defined as part of
 * the VM arguments: -Djava.security.auth.login.config=etc/login.config
 * 
 * @author daniel
 *
 */
public class BankApplicationSecurityTest {
	
	static LoginContext loginContext = null;
	private static Context context;
	static BankApplicationRemote bankApplication;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Properties props = new Properties();
		props.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		props.put("java.naming.provider.url", "localhost:1099");
		
		context = new InitialContext(props);

		bankApplication = (BankApplicationRemote) context.lookup("BankApplication/remote");
//		bankApplication.populateDatabase();
//		bankApplication.clearData();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		bankApplication.remove();
	}

	@Before
	public void setUp() throws Exception {
		loginAsUser();
	}

	@After
	public void tearDown() throws Exception {
//		bankApplication.clearData();
		logout();
	}
	
	private void loginAsUser() {
		String login = "user";
		try {
			login(login, login);
		} catch (LoginException e) {
			fail("Could not login as "+login+": "+e.getMessage());
		}
	}
	
	private void loginAsClerk() {
		String login = "clerk";
		try {
			login(login, login);
		} catch (LoginException e) {
			fail("Could not login as "+login+": "+e.getMessage());
		}
	}
	
	private void loginAsAdmin() {
		String login = "admin";
		try {
			login(login, login);
		} catch (LoginException e) {
			fail("Could not login as "+login+": "+e.getMessage());
		}
	}
	
	@Test
	public void testInvalidUser() throws LoginException {
		logout();
		login("notauser", "notapassword");
		try {
			bankApplication.createCustomer("asdf", "asdf", "", "", "", Gender.MALE, "CH");
			fail("EJBException expected.");
		} catch (EJBException e) {
			assertTrue(e.getMessage().contains("Invalid User"));
		}
	}
	
	@Test
	public void testRoles() throws LoginException {
		logout();
		loginAsAdmin();
		assertTrue(bankApplication.isInRole("administrator"));
		logout();
		loginAsClerk();
		assertTrue(bankApplication.isInRole("clerk"));
		logout();
		loginAsUser();
		assertTrue(bankApplication.isInRole("user"));
	}

//	@Test
//	public void createCustomer_notAllowedByUser() {
//		loginAsUser();
//		Customer customer = createCustomer("Hans", "Lustig");
//		assertNotNull(customer);
//	}

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
	
	private void reloadBankApplicationBean() {
		try {
			bankApplication = (BankApplicationRemote) context.lookup("BankApplication/remote");
		} catch (NamingException e) {
			fail("Could not reload bank application: "+e.getMessage());
		}
	}

	Account createDefaultAccount(Customer customer) {
		return createAccount(customer, 0.0);
	}
	
	Account createAccount(Customer customer, double balance) {
		Account account = bankApplication.createAccount(balance, Account.Type.PRIVATE_DEBIT, 1.25f, -1000.0, customer);
		return account;
	}

	Customer createCustomer(String firstName, String lastName) {
		Customer customer = bankApplication.createCustomer(firstName, "1111", firstName, lastName, "Lustiggasse 5, " +
				"9999 Lustigburg", Customer.Gender.MALE, "CH");
		return customer;
	}
}
