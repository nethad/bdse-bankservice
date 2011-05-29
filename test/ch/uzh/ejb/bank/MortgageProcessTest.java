package ch.uzh.ejb.bank;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.uzh.ejb.bank.client.MortgageProcessImpl;
import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;
import ch.uzh.ejb.bank.process.MortgageProcess;

/**
 * Functional tests for MortgageProcess
 * 
 * @author daniel
 *
 */
public class MortgageProcessTest extends BankApplicationBaseTestCase {
	
	MortgageProcess mortgageProcess;
	
	@Before
	public void setUp() throws Exception {
		login("clerk", "clerk");
	}

	@After
	public void tearDown() throws Exception {
		logout();
	}
	
	@Test
	public void defaultProcessTest() throws Exception {
		final Customer c = createCustomer("Hansruedi", "Flückiger");
		final Account a = createAccount(c, 30000.0);
		final BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(("y").getBytes())));
		mortgageProcess = new MortgageProcessImpl(bankApplication, c.getCustomerId(), a.getAccountId()) {
			
			protected BufferedReader getReader() {
				return br;
			};
			
			public void execute() {
				super.execute();
				Assert.assertTrue(application.isApproved() && application.isSigned() && !application.isClosed());
			};
		};
		mortgageProcess.execute();
	}
	
	@Test
	public void nullCustomerTest() throws Exception {
		final Customer c = createCustomer("Leonard", "Cohen");
		final Account a = createAccount(c, 30000.0);
		final BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(((c.getCustomerId() + "\ny")).getBytes())));
		mortgageProcess = new MortgageProcessImpl(bankApplication, -1, a.getAccountId()) {
			
			protected BufferedReader getReader() {
				return br;
			};
			
			public void execute() {
				super.execute();
				Assert.assertTrue(application.isApproved() && application.isSigned() && !application.isClosed());
			};
		};
		mortgageProcess.execute();
	}
	
	@Test
	public void nullAccountTest() throws Exception {
		final Customer c = createCustomer("Pirece", "Brosnan");
		final Account a = createAccount(c, 30000.0);
		final BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(((a.getAccountId() + "\ny")).getBytes())));
		mortgageProcess = new MortgageProcessImpl(bankApplication, c.getCustomerId(), -1) {
			
			protected BufferedReader getReader() {
				return br;
			};
			
			public void execute() {
				super.execute();
				Assert.assertTrue(application.isApproved() && application.isSigned() && !application.isClosed());
			};
		};
		mortgageProcess.execute();
	}
	
	@Test
	public void accountCustomerMismatchTest() throws Exception {
		final Customer c = createCustomer("Fu", "Manchu");
		final Customer c2 = createCustomer("Dr.", "Frankenstein");
		final Account a = createAccount(c, 30000.0);
		final Account a2 = createAccount(c2, 30000.0);
		final BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(((
				c.getCustomerId() + "\n" +
				a.getAccountId() + "\ny"
				)).getBytes())));
		mortgageProcess = new MortgageProcessImpl(bankApplication, c.getCustomerId(), a2.getAccountId()) {
			
			protected BufferedReader getReader() {
				return br;
			};
			
			public void execute() {
				super.execute();
				Assert.assertTrue(application.isApproved() && application.isSigned() && !application.isClosed());
			};
		};
		mortgageProcess.execute();
	}
	
	@Test
	public void customerDoesNotSignTest() throws Exception {
		final Customer c = createCustomer("Han", "Solo");
		final Account a = createAccount(c, 30000.0);
		final BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(("n").getBytes())));
		mortgageProcess = new MortgageProcessImpl(bankApplication, c.getCustomerId(), a.getAccountId()) {
			
			protected BufferedReader getReader() {
				return br;
			};
			
			public void execute() {
				super.execute();
				Assert.assertTrue(application.isApproved() && !application.isSigned() && application.isClosed());
			};
		};
		mortgageProcess.execute();
	}
	
	@Test
	public void mortgageNotAproovedTest() throws Exception {
		final Customer c = createCustomer("Luke", "Skywalker");
		final Account a = createAccount(c, 10000.0);
		final BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(("y").getBytes())));
		mortgageProcess = new MortgageProcessImpl(bankApplication, c.getCustomerId(), a.getAccountId()) {
			
			protected BufferedReader getReader() {
				return br;
			};
			
			public void execute() {
				super.execute();
				Assert.assertTrue(!application.isApproved() && !application.isSigned() && application.isClosed());
			};
		};
		mortgageProcess.execute();
	}
	
	@Test
	public void customerInfoIncompleteTest() throws Exception {
		final Customer c = bankApplication.createCustomer("bigboss99", "1234", "", "", "Strassenstrasse 56", Customer.Gender.OTHER, "CH");
		final Account a = createAccount(c, 30000.0);
		final BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(("Big\nBoss\ny").getBytes())));
		mortgageProcess = new MortgageProcessImpl(bankApplication, c.getCustomerId(), a.getAccountId()) {
			
			protected BufferedReader getReader() {
				return br;
			};
			
			public void execute() {
				super.execute();
				Assert.assertTrue(application.isApproved() && application.isSigned() && !application.isClosed());
			};
		};
		mortgageProcess.execute();
	}
}
