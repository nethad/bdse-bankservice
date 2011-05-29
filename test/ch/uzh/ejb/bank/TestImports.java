package ch.uzh.ejb.bank;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author thomas
 *
 */
public class TestImports extends BankApplicationBaseTestCase {
	
	static BankApplicationRemote bankApplication2;

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
		assertEquals("admin", bankApplication.getCustomer(100).getUserName());
		assertEquals("clerk", bankApplication.getCustomer(101).getUserName());
		assertEquals("user", bankApplication.getCustomer(102).getUserName());
	}
}
