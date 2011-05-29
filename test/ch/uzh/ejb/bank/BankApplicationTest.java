package ch.uzh.ejb.bank;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.ejb.EJBException;
import javax.security.auth.login.LoginException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;
import ch.uzh.ejb.bank.impl.utils.AccountHistoryUtil;

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
	public void testCreateAndGetAccount() throws Exception {
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
	public void testDeposit() throws Exception {
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
	public void testWithdraw() throws Exception {
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
	public void testTransfer() throws Exception {
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
	public void testTransferWithRollback() throws Exception {
		Customer customer = createCustomer("Hans", "Lustig");
		Customer customer2 = createCustomer("Fridolin", "Fisch");
		Account account = createAccount(customer, 200.0);
		Account account2 = createAccount(customer2, 0.0);
		
		try {
			bankApplication.transfer(account, account2, -1.0);
			fail("Exception expected");
		} catch (Exception e) {
			account = bankApplication.getAccount(account.getAccountId());
			account2 = bankApplication.getAccount(account2.getAccountId());
			assertEquals(200.0, account.getBalance(), 0.01);
			assertEquals(0.0, account2.getBalance(), 0.01);
		}
	}
	
	@Test
	public void withdrawFailWithRollback() throws Exception {
		Customer customer = createCustomer("Hans", "Lustig");
		double initialBalance = 200.0;
		Account account = createAccount(customer, initialBalance);
		try {
			bankApplication.withdrawFailWithRollback(account, 100.0);
			fail("EJBException expected.");
		} catch (Exception e) {
			account = bankApplication.getAccount(account.getAccountId());
			assertEquals(initialBalance, account.getBalance(), 0.01);
		}
	}
	
	@Test
	public void selectAccount() throws Exception {
		Customer userCustomer = getDefaultUserCustomer();
		Account account = createAccount(userCustomer, 200.0);
		logout();
		loginAsUser();
		long accountId = account.getAccountId();
		bankApplication.selectAccount(accountId);
		assertEquals(accountId, bankApplication.getSelectedAccountId());
	}
	
	@Test
	public void totalBalanceTest() throws Exception {
		Customer customer = createCustomer("Aaron", "Aal");
		createAccount(customer, 200.0);
		assertEquals(200.0, bankApplication.getTotalBalance(customer), 0.1);
		Account account = createAccount(customer, 0.0);
		bankApplication.withdraw(account, 1000.0);
		bankApplication.deposit(account, 500.0);
		assertEquals(-300.0, bankApplication.getTotalBalance(customer), 0.1);
	}
	
	@Test
	public void incomeTest() throws Exception {
		Customer customer = createCustomer("Berta", "Braun");
		Account account = createAccount(customer, 200.0);
		assertEquals(200.0, bankApplication.getIncome(customer, new Date(0), new Date()), 0.1);
		bankApplication.deposit(account, 1.0);
		assertEquals(201.0, bankApplication.getIncome(customer, new Date(0), new Date()), 0.1);
		bankApplication.withdraw(account, 1.0);
		assertEquals(201.0, bankApplication.getIncome(customer, new Date(0), new Date()), 0.1);
		Account account2 = createAccount(customer, 0.0);
		bankApplication.deposit(account2, 1.0);
		assertEquals(202.0, bankApplication.getIncome(customer, new Date(0), new Date()), 0.1);
	}
	
	@Test
	public void netChangeTest() throws Exception {
		Customer customer = createCustomer("Charlie", "Chaplin");
		Account account = createAccount(customer, 200.0);
		assertEquals(200.0, bankApplication.getNetChange(customer, new Date(0), new Date()), 0.1);
		bankApplication.deposit(account, 1.0);
		assertEquals(201.0, bankApplication.getNetChange(customer, new Date(0), new Date()), 0.1);
		bankApplication.withdraw(account, 1.0);
		assertEquals(200.0, bankApplication.getNetChange(customer, new Date(0), new Date()), 0.1);
		Account account2 = createAccount(customer, 0.0);
		bankApplication.withdraw(account2, 1.0);
		assertEquals(199.0, bankApplication.getNetChange(customer, new Date(0), new Date()), 0.1);
	}
	
	@Test
	public void accountHistoryTest() throws Exception {
		
		Date from = new Date();
		
		Customer userCustomer = getDefaultUserCustomer();
		Account account = createAccount(userCustomer, 200.0);
		Account account2 = createAccount(createCustomer("Al", "Capone"), 1000000000.0);
		bankApplication.setAccountStatus(account, Account.Status.OPEN);
		bankApplication.deposit(account, 10000.0);
		logout();
		loginAsUser();
		bankApplication.transfer(account, account2, 245.65);
		bankApplication.transfer(account, account2, 10.95);
		bankApplication.transfer(account, account2, 1.20);
		logout();
		loginAsAdmin();
		bankApplication.setAccountStatus(account, Account.Status.CLOSED);
		logout();
		loginAsUser();
		Date to = new Date();
		String history = bankApplication.getAccountHistory(account, from, to);
		
		StringBuilder expected = new StringBuilder(AccountHistoryUtil.HISTORY_HEADER);
		expected.append('\n');
		expected.append(AccountHistoryUtil.SEPERATOR);
		expected.append('\n');
		expected.append(AccountHistoryUtil.HISTORY_CREATED);
		expected.append('\n');
		expected.append(AccountHistoryUtil.HISTORY_DEPOSIT);
		expected.append("200.0");
		expected.append('\n');
		expected.append(AccountHistoryUtil.HISTORY_OPENED);
		expected.append('\n');
		expected.append(AccountHistoryUtil.HISTORY_DEPOSIT);
		expected.append("10000.0");
		expected.append('\n');
		expected.append(AccountHistoryUtil.HISTORY_WITHDRAWAL);
		expected.append("245.65");
		expected.append('\n');
		expected.append(AccountHistoryUtil.HISTORY_WITHDRAWAL);
		expected.append("10.95");
		expected.append('\n');
		expected.append(AccountHistoryUtil.HISTORY_WITHDRAWAL);
		expected.append("1.2");
		expected.append('\n');
		expected.append(AccountHistoryUtil.HISTORY_CLOSED);
		
		assertEquals(expected.toString(), history);
	}
}
