package ch.uzh.ejb.bank;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import javax.ejb.EJBException;
import javax.security.auth.login.LoginException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;
import ch.uzh.ejb.bank.entities.Account.Status;
import ch.uzh.ejb.bank.entities.Customer.Gender;

/**
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
	public void testInvalidUser() throws Exception {
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
	
	@Test
	public void testCreateCustomer_inUserRole() throws Exception {
		loginAsUser();
		try {
			createCustomer("heinrich");
			fail("EJBException expected");
		} catch (EJBException e) {
			assertTrue(e.getMessage().contains("Caller unauthorized"));
		}
	}
	
	@Test
	public void testCreateAccount_inUserRole() throws Exception {
		loginAsUser();
		try {
			createAccount(200.0);
			fail("Exception expected");
		} catch (EJBException e) {
			assertTrue(e.getMessage().contains("Caller unauthorized"));
		}
	}
	
	@Test
	public void testDeposit_accountOwner() throws Exception {
		loginAsClerk();
		Customer customer = bankApplication.getCustomerByUsername("user");
		long customerId = customer.getCustomerId();
		bankApplication.selectCustomer(customerId);
		Account account = createAccount(200.0);
		loginAsUser();
		bankApplication.setDefaultCustomer();
		try {
			Account account2 = bankApplication.getAccount(account.getAccountId());
			bankApplication.deposit(account2, 100.0);
			fail("EJBException expected");
		} catch (EJBException e) {
			assertTrue(e.getMessage().contains("Caller unauthorized"));
		}
	}
	
	@Test
	public void testDeposit_notAccountOwner() throws Exception {
		loginAsClerk();
		Customer customer = createCustomer("heinz");
		long customerId = customer.getCustomerId();
		bankApplication.selectCustomer(customerId);
		Account account = createAccount(200.0);
		loginAsUser();
		bankApplication.setDefaultCustomer();
		try {
			Account account2 = bankApplication.getAccount(account.getAccountId());
			bankApplication.deposit(account2, 100.0);
			fail("EJBException expected");
		} catch (EJBException e) {
			assertTrue(e.getMessage().contains("Caller unauthorized"));
		}
	}
	
	@Test
	public void testWithdraw_accountOwner() throws Exception {
		loginAsClerk();
		Customer customer = bankApplication.getCustomerByUsername("user");
		long customerId = customer.getCustomerId();
		bankApplication.selectCustomer(customerId);
		Account account = createAccount(200.0);
		loginAsUser();
		bankApplication.setDefaultCustomer();
		try {
			Account account2 = bankApplication.getAccount(account.getAccountId());
			bankApplication.withdraw(account2, 100.0);
			fail("EJBException expected");
		} catch (EJBException e) {
			assertTrue(e.getMessage().contains("Caller unauthorized"));
		}
	}
	
	@Test
	public void testWithdraw_notAccountOwner() throws Exception {
		loginAsClerk();
		Customer customer = createCustomer("hans");
		long customerId = customer.getCustomerId();
		bankApplication.selectCustomer(customerId);
		Account account = createAccount(200.0);
		loginAsUser();
		bankApplication.setDefaultCustomer();
		try {
			Account account2 = bankApplication.getAccount(account.getAccountId());
			bankApplication.withdraw(account2, 100.0);
			fail("EJBException expected");
		} catch (EJBException e) {
			assertTrue(e.getMessage().contains("Caller unauthorized"));
		}
	}
	
	@Test
	public void testTransfer_notAccountOwner() throws Exception {
		loginAsClerk();
		Customer customer = createCustomer("holger");
		long customerId = customer.getCustomerId();
		bankApplication.selectCustomer(customerId);
		Account sourceAccount = createAccount(200.0);
//		long sourceAccountId = sourceAccount.getAccountId();
		Account targetAccount = createAccount(bankApplication.getCustomerByUsername("user"), 200.0);
		long targetAccountId = targetAccount.getAccountId();
		loginAsUser();
		bankApplication.setDefaultCustomer();
		try {
			targetAccount = bankApplication.getAccount(targetAccountId);
			bankApplication.transfer(sourceAccount, targetAccount, 100.0);
			fail("Exception expected");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("You are not owner of this account."));
		}
	}
	
	@Test
	public void testAccountHistory_notAccountOwner() throws Exception {
		loginAsClerk();
		Customer customer = createCustomer("ivan");
		Account account = createAccount(customer, 200.0);
		loginAsUser();
		try {
			bankApplication.getAccountHistory(account, new Date(), new Date());
			fail("Exception expected");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("You are not owner of this account."));
		}
	}
	
	@Test
	public void testCloseAccount_notAccountOwner() throws Exception {
		loginAsClerk();
		Customer customer = createCustomer("isabel");
		Account account = createAccount(customer, 200.0);
		loginAsUser();
		try {
			bankApplication.setAccountStatus(account, Status.CLOSED);
			fail("Exception expected");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("You are not owner of this account."));
		}
	}
	
}
