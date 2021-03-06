package ch.uzh.ejb.bank;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;
import ch.uzh.ejb.bank.entities.Portfolio;
import ch.uzh.ejb.bank.impl.utils.AccountHistoryUtil;

/**
 * Generic functional tests the public interface of BankApplication.
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
	public void testCreateAndGetCustomer() throws Exception {
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
	public void accountCustomerRelationshipTest() throws Exception {
		Customer customer = createCustomer("Lena", "Luna");
		Account account = createDefaultAccount(customer);
		
		Customer customerUpdated = bankApplication.getCustomer(customer.getCustomerId());
		assertTrue(customerUpdated.getAccounts().contains(account));
		assertEquals(account.getCustomer(), customer);
		
		Account account2 = createDefaultAccount(customer);
		customerUpdated = bankApplication.getCustomer(customer.getCustomerId());
		assertTrue(customerUpdated.getAccounts().contains(account2));
		assertTrue(customerUpdated.getAccounts().contains(account));
		assertEquals(account2.getCustomer(), customer);
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
		bankApplication.selectCustomer(customer.getCustomerId());
		assertEquals(200.0, bankApplication.getTotalBalance(), 0.1);
		Account account = createAccount(customer, 0.0);
		bankApplication.withdraw(account, 1000.0);
		bankApplication.deposit(account, 500.0);
		assertEquals(-300.0, bankApplication.getTotalBalance(), 0.1);
	}
	
	@Test
	public void incomeTest() throws Exception {
		Customer customer = createCustomer("Berta", "Braun");
		Account account = createAccount(customer, 200.0);
		bankApplication.selectCustomer(customer.getCustomerId());
		assertEquals(200.0, bankApplication.getIncome(new Date(0), new Date()), 0.1);
		bankApplication.deposit(account, 1.0);
		assertEquals(201.0, bankApplication.getIncome(new Date(0), new Date()), 0.1);
		bankApplication.withdraw(account, 1.0);
		assertEquals(201.0, bankApplication.getIncome(new Date(0), new Date()), 0.1);
		Account account2 = createAccount(customer, 0.0);
		bankApplication.deposit(account2, 1.0);
		assertEquals(202.0, bankApplication.getIncome(new Date(0), new Date()), 0.1);
	}
	
	@Test
	public void netChangeTest() throws Exception {
		Customer customer = createCustomer("Charlie", "Chaplin");
		Account account = createAccount(customer, 200.0);
		bankApplication.selectCustomer(customer.getCustomerId());
		assertEquals(200.0, bankApplication.getNetChange(new Date(0), new Date()), 0.1);
		bankApplication.deposit(account, 1.0);
		assertEquals(201.0, bankApplication.getNetChange(new Date(0), new Date()), 0.1);
		bankApplication.withdraw(account, 1.0);
		assertEquals(200.0, bankApplication.getNetChange(new Date(0), new Date()), 0.1);
		Account account2 = createAccount(customer, 0.0);
		bankApplication.withdraw(account2, 1.0);
		assertEquals(199.0, bankApplication.getNetChange(new Date(0), new Date()), 0.1);
	}
	
	@Test
	public void portfolioTest() throws Exception {
		Customer customer = createCustomer("Fritz", "Freitag");
		bankApplication.selectCustomer(customer.getCustomerId());
		Portfolio portfolio = bankApplication.getCustomerPortfolio();
		assertTrue(0 == portfolio.getShares().size());
		assertEquals(customer, portfolio.getCustomer());
	}
	
	@Test
	public void shareTest() throws Exception {
		Customer customer = createCustomer("Michael", "Mittwoch");
		bankApplication.selectCustomer(customer.getCustomerId());
		Account account = createAccount(1000.0);
			
		Customer customer2 = createCustomer("Donald", "Donnerstag");
		bankApplication.selectCustomer(customer2.getCustomerId());
		Account account2 = createAccount(1000.0);
		bankApplication.addShare("APP", 100, 100.00);
		Portfolio portfolio2 = bankApplication.getCustomerPortfolio();
		assertTrue(portfolio2.getShares().size() == 1);
		assertTrue(portfolio2.getShares().get(0).getSymbol().equals("APP"));
		assertTrue(portfolio2.getShares().get(0).getQuantity() == 100);
		assertTrue(portfolio2.getShares().get(0).getAveragePurchasePrice() == 100.0);
		bankApplication.addShare("APP", 100, 200.0);
		portfolio2 = bankApplication.getCustomerPortfolio();
		assertTrue(portfolio2.getShares().size() == 1);
		assertTrue(portfolio2.getShares().get(0).getAveragePurchasePrice() == 150.0);
		assertTrue(portfolio2.getShares().get(0).getQuantity() == 200);
		bankApplication.addShare("MCS", 10, 101.54);
		portfolio2 = bankApplication.getCustomerPortfolio();
		assertTrue(portfolio2.getShares().size() == 2);
		bankApplication.removeShare("MCS", 10);
		portfolio2 = bankApplication.getCustomerPortfolio();
		assertTrue(portfolio2.getShares().size() == 1);
		bankApplication.removeShare("APP", 150);
		portfolio2 = bankApplication.getCustomerPortfolio();
		assertTrue(portfolio2.getShares().size() == 1);
		assertTrue(portfolio2.getShares().get(0).getQuantity() == 50);
	
		bankApplication.selectCustomer(customer.getCustomerId());
		Portfolio portfolio = bankApplication.getCustomerPortfolio();
		assertTrue(portfolio.getShares().size() == 0);
		
		bankApplication.selectCustomer(customer2.getCustomerId());
		bankApplication.selectAccount(account2.getAccountId());
		bankApplication.transferShare(account.getAccountId(), "APP", 25, 500.0);
		portfolio2 = bankApplication.getCustomerPortfolio();
		assertTrue(portfolio2.getShares().size() == 1);
		assertTrue(portfolio2.getShares().get(0).getQuantity() == 25);
		account2 = bankApplication.getAccount(account2.getAccountId());
		assertTrue(account2.getBalance() == 1500.0);
		
		bankApplication.selectCustomer(customer.getCustomerId());
		bankApplication.selectAccount(account.getAccountId());
		portfolio = bankApplication.getCustomerPortfolio();
		account = bankApplication.getAccount(account.getAccountId());
		assertTrue(portfolio2.getShares().size() == 1);
		assertTrue(portfolio2.getShares().get(0).getQuantity() == 25);
		assertTrue(account.getBalance() == 500.0);
	}
	
	@Test
	public void accountHistoryTest() throws Exception {
		
		Date from = new Date();
		
		Customer userCustomer = getDefaultUserCustomer();
		Account account = createAccount(userCustomer, 200.0);
		Account account2 = createAccount(createCustomer("Al", "Capone"), 1000000000.0);
		bankApplication.selectAccount(account.getAccountId());
		bankApplication.setAccountStatus(Account.Status.OPEN);
		bankApplication.deposit(10000.0);
		logout();
		loginAsUser();
		bankApplication.transfer(account2.getAccountId(), 245.65);
		bankApplication.transfer(account2.getAccountId(), 10.95);
		bankApplication.transfer(account2.getAccountId(), 1.20);
		logout();
		loginAsAdmin();
		bankApplication.setAccountStatus(Account.Status.CLOSED);
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
