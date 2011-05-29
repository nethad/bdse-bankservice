package ch.uzh.ejb.bank;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author daniel
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
	BankApplicationTest.class,
	BankApplicationSecurityTest.class,
	BankApplicationStatefulTest.class,
	TestImports.class,
	MortgageProcessTest.class
	})
public class AllTests {}
