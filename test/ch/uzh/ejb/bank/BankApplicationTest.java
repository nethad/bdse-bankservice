package ch.uzh.ejb.bank;

import static org.junit.Assert.*;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * For security to work, the following key value pair must be defined as part of
 * the VM arguments: -Djava.security.auth.login.config=etc/login.config
 * 
 * @author daniel
 *
 */
public class BankApplicationTest {
	
	static BankApplicationRemote bankApplication;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
//		Properties p = new Properties();
//        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
//        p.put("test-db", "new://Resource?type=DataSource");
//        p.put("test-db.JdbcDriver", "org.hsqldb.jdbcDriver");
//        p.put("test-db.JdbcUrl", "jdbc:hsqldb:mem:testdb");
//        p.put("test-db.JdbcUrl.openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");

		Properties props = new Properties();
		props.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		props.put("java.naming.provider.url", "localhost:1099");
		
		context = new InitialContext(props);

		bankApplication = (BankApplicationRemote) context.lookup("BankApplication/remote");
		bankApplication.populateDatabase();
		bankApplication.clearData();
//        bankApplication = (BankApplicationRemote) context.lookup("BankApplicationRemote");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		bankApplication.remove();
	}

	@Before
	public void setUp() throws Exception {
		login("admin", "admin");
	}

	@After
	public void tearDown() throws Exception {
		bankApplication.clearData();
		logout();
	}

	@Test
	public void testCreateAndGetCustomer() {
		Customer customer = createCustomer("Hans", "Lustig");
		assertNotNull(customer);
		Customer customer2 = bankApplication.getCustomer(customer.getCustomerId());
		assertNotNull(customer2);
		assertEquals(customer.getCustomerId(), customer2.getCustomerId());
		Customer customer3 = bankApplication.getCustomer(customer.getFirstName(), customer.getLastName()).get(0);
		assertNotNull(customer3);
		assertEquals(customer.getCustomerId(), customer3.getCustomerId());
	}

	@Test
	public void testCreateAndGetAccount() {
		System.out.println(loginContext.getSubject());
		
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
		
		bankApplication.transfer(account, account2, -1.0);
		account = bankApplication.getAccount(account.getAccountId());
		account2 = bankApplication.getAccount(account2.getAccountId());
		assertEquals(200.0, account.getBalance(), 0.01);
		assertEquals(0.0, account2.getBalance(), 0.01);
	}
	
	@Test
	public void withdrawFailWithRollback() {
		Customer customer = createCustomer("Hans", "Lustig");
		Account account = createAccount(customer, 200.0);
		
		bankApplication.withdrawFailWithRollback(account, 100.0);
		account = bankApplication.getAccount(account.getAccountId());
		assertEquals(200.0, account.getBalance(), 0.01);
	}

	static LoginContext loginContext = null;
	private static Context context;

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
