package ch.uzh.ejb.bank.impl;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.jboss.ejb3.annotation.SecurityDomain;

import ch.uzh.ejb.bank.BankApplicationTestRemote;
import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;

/**
 * Unit-Test access bean implementation.
 * 
 * @author daniel
 *
 */
@Stateful
@SecurityDomain("bankapplication")
@DeclareRoles({"administrator, clerk, user"})
@TransactionManagement(TransactionManagementType.CONTAINER)
public class BankApplicationTestBean extends BankApplication implements BankApplicationTestRemote {
	
	@Override
	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void withdrawFailWithRollback(Account account, double value) throws Exception {
		withdraw(account, value);
		context.setRollbackOnly();
		throw new Exception("This transaction will always fail. (Proof of concept)");
	}
	
	@Override
	public boolean isInRole(String role) {
		return context.isCallerInRole(role);
	}
	
	@Override
	public Customer getCustomerByUsername(String userName) {
		return super.getCustomerByUsername(userName);
	}
	
	
}
