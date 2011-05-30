package ch.uzh.ejb.bank.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Query;

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
	
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	@SuppressWarnings("unchecked")
	public List<Account> getAccounts(Customer customer) {
		List<Account> accounts = null;
		if (customer != null) {
			Query q = em.createNamedQuery("Account.findByCustomer");
			q.setParameter("customer", customer);
			try {
				accounts = q.getResultList();
			} catch (Exception ex) {
				accounts = new ArrayList<Account>();
			}
		}
		return accounts;
	}
	
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Account deposit(Account toAccount, double value) throws Exception {
		return deposit_intern(toAccount, value);
	}
	
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Account withdraw(Account fromAccount, double value) throws Exception {
		return withdraw_intern(fromAccount, value);
	}
	
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void transfer(Account fromAccount, Account toAccount, double value) throws Exception {
		transfer_intern(fromAccount, toAccount, value);
	}
	
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	public String getAccountHistory(Account account, Date from, Date to) throws Exception {
		return getAccountHistory_intern(account, from, to);
	}
}
