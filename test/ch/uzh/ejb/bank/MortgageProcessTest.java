package ch.uzh.ejb.bank;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MortgageProcessTest extends BankApplicationBaseTestCase {
	@Before
	public void setUp() throws Exception {
		login("admin", "admin");
	}

	@After
	public void tearDown() throws Exception {
		logout();
	}
	
	@Test
	public void test() {
	}
}
