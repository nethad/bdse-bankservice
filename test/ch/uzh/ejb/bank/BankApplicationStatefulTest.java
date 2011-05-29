package ch.uzh.ejb.bank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;

public class BankApplicationStatefulTest extends BankApplicationBaseTestCase {
	
	@Before
	public void setUp() throws Exception {
		login("admin", "admin");
	}

	@After
	public void tearDown() throws Exception {
		logout();
	}
	
	@Test
	public void testDeposit() throws Exception {
		Customer customer = createCustomer("Hans", "Lustig");
		
		long customerId = customer.getCustomerId();
		
		bankApplication.selectCustomer(customerId);
		Account account = createAccount(0.0);
		long accountId = account.getAccountId();
		bankApplication.selectAccount(accountId);
		
		account = bankApplication.deposit(100.0);
		
		assertEquals(100.0, account.getBalance(), 0.01);
		Account account2 = bankApplication.getAccount(accountId);
		assertEquals(100.0, account2.getBalance(), 0.01);
		
		account.setBalance(0);
		account2 = bankApplication.getAccount(accountId);
		assertEquals(100.0, account2.getBalance(), 0.01);
		
		account = bankApplication.deposit(100.0);
		assertEquals(200.0, account.getBalance(), 0.01);
		account2 = bankApplication.getAccount(accountId);
		assertEquals(200.0, account2.getBalance(), 0.01);
	}
	
	@Test
	public void testDeposit_negativeAmount() throws Exception {
		Customer customer = createCustomer("Hans", "Lustig");
		
		long customerId = customer.getCustomerId();
		
		bankApplication.selectCustomer(customerId);
		Account account = createAccount(0.0);
		long accountId = account.getAccountId();
		bankApplication.selectAccount(accountId);
		
		try {
			account = bankApplication.deposit(-100.0);
			fail("Exception expected");
		} catch (Exception e) {
			Account account2 = bankApplication.getAccount(accountId);
			assertEquals(0.0, account2.getBalance(), 0.01);
		}
	}
	
	@Test
	public void testWithdraw() throws Exception {
		Customer customer = createCustomer("Hans", "Lustig");
		bankApplication.selectCustomer(customer.getCustomerId());
		
		Account account = createAccount(200.0);
		long accountId = account.getAccountId();
		bankApplication.selectAccount(accountId);
		
		account = bankApplication.withdraw(100.0);
		assertEquals(100.0, account.getBalance(), 0.01);
		Account account2 = bankApplication.getAccount(accountId);
		assertEquals(100.0, account2.getBalance(), 0.01);
		
		account = bankApplication.withdraw(100.0);
		assertEquals(0.0, account.getBalance(), 0.01);
		account2 = bankApplication.getAccount(accountId);
		assertEquals(0.0, account2.getBalance(), 0.01);
	}
	
	@Test
	public void testWithdraw_negativeAmount() throws Exception {
		Customer customer = createCustomer("Hans", "Lustig");
		bankApplication.selectCustomer(customer.getCustomerId());
		
		Account account = createAccount(200.0);
		long accountId = account.getAccountId();
		bankApplication.selectAccount(accountId);
		
		try {
			account = bankApplication.withdraw(-100.0);
			fail("Exception expected");
		} catch (Exception e) {
			Account account2 = bankApplication.getAccount(accountId);
			assertEquals(200.0, account2.getBalance(), 0.01);
		}
	}
	
	@Test
	public void testWithdraw_limit() throws Exception {
		Customer customer = createCustomer("Hans", "Lustig");
		bankApplication.selectCustomer(customer.getCustomerId());
		
		Account account = createAccount(200.0);
		long accountId = account.getAccountId();
		bankApplication.selectAccount(accountId);

		assertEquals(-1000.0, account.getCreditLimit(), 0.01);
		account = bankApplication.withdraw(1200.0);
	}
	
	@Test
	public void testWithdraw_overLimit() throws Exception {
		Customer customer = createCustomer("Hans", "Lustig");
		bankApplication.selectCustomer(customer.getCustomerId());
		
		Account account = createAccount(200.0);
		long accountId = account.getAccountId();
		bankApplication.selectAccount(accountId);

		assertEquals(-1000.0, account.getCreditLimit(), 0.01);
		try {
			account = bankApplication.withdraw(1201.0);
			fail("Exception expected");
		} catch (Exception e) {
			Account account2 = bankApplication.getAccount(accountId);
			assertEquals(200.0, account2.getBalance(), 0.01);
		}
	}
	
	@Test
	public void testTransfer() throws Exception {
		Customer customerOne = createCustomer("Hans", "Lustig");
		long customerOneId = customerOne.getCustomerId();
		bankApplication.selectCustomer(customerOneId);
		Account accountOne = createAccount(200.0);
		long accountOneId = accountOne.getAccountId();
		
		Customer customerTwo = createCustomer("Fridolin", "Fisch");
		long customerTwoId = customerTwo.getCustomerId();
		bankApplication.selectCustomer(customerTwoId);
		Account accountTwo = createAccount(0.0);
		long accountTwoId = accountTwo.getAccountId();
		
		bankApplication.selectAccount(accountOneId);
		
		bankApplication.transfer(accountTwoId, 200.0);
		accountOne = bankApplication.getAccount(accountOneId);
		accountTwo = bankApplication.getAccount(accountTwoId);
		assertEquals(0.0, accountOne.getBalance(), 0.01);
		assertEquals(200.0, accountTwo.getBalance(), 0.01);
	}
	
	@Test
	public void testTransferWithRollback_negativeAmount() throws Exception {
		Customer customerOne = createCustomer("Hans", "Lustig");
		long customerOneId = customerOne.getCustomerId();
		bankApplication.selectCustomer(customerOneId);
		Account accountOne = createAccount(200.0);
		long accountOneId = accountOne.getAccountId();
		
		Customer customerTwo = createCustomer("Fridolin", "Fisch");
		long customerTwoId = customerTwo.getCustomerId();
		bankApplication.selectCustomer(customerTwoId);
		Account accountTwo = createAccount(0.0);
		long accountTwoId = accountTwo.getAccountId();
		
		try {
			bankApplication.transfer(accountTwoId, -1.0);
			fail("Exception expected");
		} catch (Exception e) {
			accountOne = bankApplication.getAccount(accountOneId);
			accountTwo = bankApplication.getAccount(accountTwoId);
			assertEquals(200.0, accountOne.getBalance(), 0.01);
			assertEquals(0.0, accountTwo.getBalance(), 0.01);
		}
	}
	
	@Test
	public void testTransferWithRollback_nonexistantTargetAccount() throws Exception {
		Customer customerOne = createCustomer("Hans", "Lustig");
		long customerOneId = customerOne.getCustomerId();
		bankApplication.selectCustomer(customerOneId);
		Account accountOne = createAccount(200.0);
		long accountOneId = accountOne.getAccountId();
		
		try {
			bankApplication.selectAccount(accountOneId);
			bankApplication.transfer(123456789, -1.0);
			fail("Exception expected");
		} catch (Exception e) {
			accountOne = bankApplication.getAccount(accountOneId);
			assertEquals(200.0, accountOne.getBalance(), 0.01);
		}
	}
	
	@Test
	public void testTransfer_highestAmountTransferredFromSourceAccount() throws Exception {
		Customer customerOne = createCustomer("Hans", "Lustig");
		long customerOneId = customerOne.getCustomerId();
		bankApplication.selectCustomer(customerOneId);
		Account accountOne = createAccount(200.0);
		long accountOneId = accountOne.getAccountId();
		
		Customer customerTwo = createCustomer("Fridolin", "Fisch");
		long customerTwoId = customerTwo.getCustomerId();
		bankApplication.selectCustomer(customerTwoId);
		Account accountTwo = createAccount(0.0);
		long accountTwoId = accountTwo.getAccountId();
		
		bankApplication.selectAccount(accountOneId);
		
		assertEquals(-1000.0, accountOne.getCreditLimit(), 0.01);
		bankApplication.transfer(accountTwoId, 1200.0);
		accountOne = bankApplication.getAccount(accountOneId);
		accountTwo = bankApplication.getAccount(accountTwoId);
		assertEquals(-1000.0, accountOne.getBalance(), 0.01);
		assertEquals(1200.0, accountTwo.getBalance(), 0.01);
	}
	
	@Test
	public void testTransfer_notEnoughMoneyOnSourceAccount() throws Exception {
		Customer customerOne = createCustomer("Hans", "Lustig");
		long customerOneId = customerOne.getCustomerId();
		bankApplication.selectCustomer(customerOneId);
		Account accountOne = createAccount(200.0);
		long accountOneId = accountOne.getAccountId();
		
		Customer customerTwo = createCustomer("Fridolin", "Fisch");
		long customerTwoId = customerTwo.getCustomerId();
		bankApplication.selectCustomer(customerTwoId);
		Account accountTwo = createAccount(0.0);
		long accountTwoId = accountTwo.getAccountId();
		
		bankApplication.selectAccount(accountOneId);
		
		assertEquals(-1000.0, accountOne.getCreditLimit(), 0.01);
		
		try {
			bankApplication.transfer(accountTwoId, 1201.0);
			fail("Exception expected");
		} catch (Exception e) {
			accountOne = bankApplication.getAccount(accountOneId);
			accountTwo = bankApplication.getAccount(accountTwoId);
			assertEquals(200.0, accountOne.getBalance(), 0.01);
			assertEquals(0.0, accountTwo.getBalance(), 0.01);
		}
	}

	private Account createAccount(double balance) throws Exception {
		Account account = bankApplication.createAccount(balance, Account.Type.PRIVATE_DEBIT, 1.25f, -1000.0);
		return account;
	}

}
