package ch.uzh.ejb.bank;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;

import javax.ejb.EJBException;
import javax.security.auth.login.LoginException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;

/**
 * Generic functional tests the public interface of BankApplication.
 * 
 * For security to work, the following key value pair must be defined as part of
 * the VM arguments: -Djava.security.auth.login.config=etc/login.config
 * 
 * @author daniel
 *
 */
public class BankApplicationTest extends BankApplicationBaseTestCase {

	@Before
	public void setUp() throws Exception {
		login("admin", "admin");
	}

	@After
	public void tearDown() throws Exception {
		logout();
	}
	
	@Test
	public void testCreateAndGetCustomer() {
		Customer customer = createCustomer("Hans", "Lustig");
		assertNotNull(customer);
		Customer customer2 = bankApplication.getCustomer(customer.getCustomerId());
		assertNotNull(customer2);
		assertEquals(customer.getCustomerId(), customer2.getCustomerId());
		List<Customer> customers = bankApplication.getCustomer(customer.getFirstName(), customer.getLastName());
		HashSet<Long> ids = new HashSet<Long>();
		for(Customer c: customers) {
			ids.add(c.getCustomerId());
		}
		assertTrue(ids.contains(customer.getCustomerId()));
	}

	@Test
	public void testCreateAndGetAccount() throws LoginException {
		assertTrue(bankApplication.isInRole("administrator"));
		
		Customer customer = createCustomer("Hans", "Lustig");
		Account account = createDefaultAccount(customer);
		assertNotNull(account);
		
		Account account2 = bankApplication.getAccount(account.getAccountId());
		assertNotNull(account2);
		assertEquals(account.getAccountId(), account2.getAccountId());
		
		Account account3 = bankApplication.getAccounts(customer).get(0);
		assertNotNull(account3);
		assertEquals(account.getAccountId(), account3.getAccountId());
	}
	
	@Test
	public void testDeposit() {
		Customer customer = createCustomer("Hans", "Lustig");
		Account account = createDefaultAccount(customer);
		account = bankApplication.deposit(account, 100.0);
		assertEquals(100.0, account.getBalance(), 0.01);
		Account account2 = bankApplication.getAccount(account.getAccountId());
		assertEquals(100.0, account2.getBalance(), 0.01);
		
		account.setBalance(0);
		account2 = bankApplication.getAccount(account.getAccountId());
		assertEquals(100.0, account2.getBalance(), 0.01);
		
		account = bankApplication.deposit(account, 100.0);
		assertEquals(200.0, account.getBalance(), 0.01);
		account2 = bankApplication.getAccount(account.getAccountId());
		assertEquals(200.0, account2.getBalance(), 0.01);
	}
	
	@Test
	public void testWithdraw() {
		Customer customer = createCustomer("Hans", "Lustig");
		Account account = createAccount(customer, 200.0);
		account = bankApplication.withdraw(account, 100.0);
		assertEquals(100.0, account.getBalance(), 0.01);
		Account account2 = bankApplication.getAccount(account.getAccountId());
		assertEquals(100.0, account2.getBalance(), 0.01);
		
		account = bankApplication.withdraw(account, 100.0);
		assertEquals(0.0, account.getBalance(), 0.01);
		account2 = bankApplication.getAccount(account.getAccountId());
		assertEquals(0.0, account2.getBalance(), 0.01);
	}
	
	@Test
	public void testTransfer() {
		Customer customer = createCustomer("Hans", "Lustig");
		Customer customer2 = createCustomer("Fridolin", "Fisch");
		Account account = createAccount(customer, 200.0);
		Account account2 = createAccount(customer2, 0.0);
		
		bankApplication.transfer(account, account2, 200.0);
		account = bankApplication.getAccount(account.getAccountId());
		account2 = bankApplication.getAccount(account2.getAccountId());
		assertEquals(0.0, account.getBalance(), 0.01);
		assertEquals(200.0, account2.getBalance(), 0.01);
	}
	
	@Test
	public void testTransferWithRollback() {
		Customer customer = createCustomer("Hans", "Lustig");
		Customer customer2 = createCustomer("Fridolin", "Fisch");
		Account account = createAccount(customer, 200.0);
		Account account2 = createAccount(customer2, 0.0);
		
		try {
			bankApplication.transfer(account, account2, -1.0);
			fail("EJBException expected");
		} catch (EJBException e) {
			reloadBankApplicationBean();
			account = bankApplication.getAccount(account.getAccountId());
			account2 = bankApplication.getAccount(account2.getAccountId());
			assertEquals(200.0, account.getBalance(), 0.01);
			assertEquals(0.0, account2.getBalance(), 0.01);
		}
	}
	
	@Test
	public void withdrawFailWithRollback() {
		Customer customer = createCustomer("Hans", "Lustig");
		double initialBalance = 200.0;
		Account account = createAccount(customer, initialBalance);
		try {
			bankApplication.withdrawFailWithRollback(account, 100.0);
			fail("EJBException expected.");
		} catch (EJBException e) {
			reloadBankApplicationBean();
			account = bankApplication.getAccount(account.getAccountId());
			assertEquals(initialBalance, account.getBalance(), 0.01);
		}
	}
	
	@Test
	public void selectAccount() throws LoginException {
		Customer userCustomer = getDefaultUserCustomer();
		Account account = createAccount(userCustomer, 200.0);
		logout();
		loginAsUser();
		bankApplication.selectAccount(account.getAccountId());
	}
}
