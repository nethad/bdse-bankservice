package ch.uzh.ejb.bank;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.ejb.EJBException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.uzh.ejb.bank.entities.Customer.Gender;

/**
 * For security to work, the following key value pair must be defined as part of
 * the VM arguments: -Djava.security.auth.login.config=etc/login.config
 * 
 * @author thomas
 *
 */
public class BankApplicationSecurityTest extends BankApplicationBaseTestCase {

	@Before
	public void setUp() throws Exception {
		loginAsUser();
	}

	@After
	public void tearDown() throws Exception {
		logout();
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
}
