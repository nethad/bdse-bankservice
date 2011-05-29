package ch.uzh.ejb.bank;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.uzh.ejb.bank.client.MortgageProcessImpl;
import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;
import ch.uzh.ejb.bank.process.MortgageProcess;

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
		Customer customer = createCustomer("Hansruedi", "Flückiger");
		Account account = createAccount(customer, 30000.0);
		mortgageProcess = new MortgageProcessImpl(bankApplication, customer.getCustomerId(), account.getAccountId()) {
			
			protected InputStream getInputStream() {
				String str = "y";
				InputStream is = new ByteArrayInputStream(str.getBytes());
				return is;
			};
		};
		mortgageProcess.execute();
	}
}
