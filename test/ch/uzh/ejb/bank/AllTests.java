package ch.uzh.ejb.bank;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * For security to work, the following key value pair must be defined as part of
 * the VM arguments: -Djava.security.auth.login.config=etc/login.config
 * 
 * @author daniel
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
	BankApplicationTest.class,
	BankApplicationSecurityTest.class,
	BankApplicationStatefulTest.class,
	TestImports.class
	})
public class AllTests {}
